package com.buaa.academic.analysis.service.impl.fpg;

import com.buaa.academic.analysis.service.impl.AnalysisServiceImpl;
import com.buaa.academic.analysis.service.impl.StatusResources;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationMapper;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationReducer;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationRule;
import com.buaa.academic.analysis.service.impl.fpg.FPGrowth.FpgMapper;
import com.buaa.academic.analysis.service.impl.fpg.FPGrowth.FpgReducer;
import com.buaa.academic.analysis.service.impl.fpg.FilterAndSort.SortMapper;
import com.buaa.academic.analysis.service.impl.fpg.FilterAndSort.SortReducer;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequency;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequentMapper;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequentReducer;
import com.buaa.academic.document.entity.Paper;
import lombok.SneakyThrows;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FPGMainClass implements Runnable{

    public static String splitChar = " ";
    private String analysisObject; // 用于区分对话题还是学科进行分析
    private String name; // 用于获取当前作业的运行状态
    private String inputPath;
    private double minSupport;
    private double minConfidence;
    private boolean deleteTmpFiles;
    private final String resultDir;
    private final String countPath;
    private final String frequentItemsPath;
    private final String frequentSetsPath;
    private final String associationRulesPath;
    private ElasticsearchRestTemplate template;
    private final Configuration configuration;

    public FPGMainClass() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        resultDir = "./modules/analysis/src/main/resources/fpgResult" + time;
        countPath =  resultDir +"\\count";
        frequentItemsPath = resultDir + "\\frequentItems";
        frequentSetsPath =  resultDir + "\\frequentSets";
        associationRulesPath = resultDir + "\\associationRules";
        configuration = new Configuration(true);
    }

    public FPGMainClass setAnalysisObject(String analysisObject) {
        this.analysisObject = analysisObject;
        this.inputPath = "./modules/analysis/src/main/resources/" + analysisObject + "Data";
        return this;
    }

    public FPGMainClass setMinSupport(double minSupport) {
        this.minSupport = minSupport;
        return this;
    }

    public FPGMainClass setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
        return this;
    }

    public FPGMainClass setDeleteTmpFiles(boolean deleteTmpFiles) {
        this.deleteTmpFiles = deleteTmpFiles;
        return this;
    }

    public FPGMainClass setTemplate(ElasticsearchRestTemplate template) {
        this.template = template;
        return this;
    }

    public FPGMainClass setName(String name) {
        this.name = name;
        return this;
    }

    @SneakyThrows
    @Override
    public void run() {

        changeRunningStatusTo("FP-Growth analysis starting...");

        long start_time = System.currentTimeMillis();

        getInputData();

        // 词频统计
        Job countJob = frequencyCal();

        // 去除非频繁项，根据词频对原数据排序
        sortItem(countJob);

        // fp-growth生成频繁项集
        fpgExc();

        // 计算关联规则
        associationCal();

        if (deleteTmpFiles) {
            deleteDir(resultDir);
            deleteDir(inputPath);
        }

        double costTime = ((double)(System.currentTimeMillis() - start_time) / 1000);
        changeRunningStatusToStop("All Down! " + "Cost " + costTime + "s");
    }

    private void changeRunningStatusTo(String runningStatus) {
        synchronized (AnalysisServiceImpl.STATUS_LOCK) {
            StatusResources.runningStatus.put(name, runningStatus);
        }
        System.out.println(name + ": " + runningStatus);
    }

    private void changeRunningStatusToStop(String runningStatus) {
        synchronized (AnalysisServiceImpl.STATUS_LOCK) {
            StatusResources.isRunning.put(name, false);
            StatusResources.runningStatus.put(name, runningStatus);
            StatusResources.currentJob.remove(name);
        }
        System.out.println(name + ": " + runningStatus);
    }

    private void getInputData() throws IOException {
        File inputFile = new File(inputPath);
        if (inputFile.exists()) {
            deleteDir(inputPath);
        }
        if (!inputFile.createNewFile()) {
            changeRunningStatusToStop("Can't make file " + inputFile);
            throw new IOException();
        }
        FileWriter fileWriter = new FileWriter(inputFile);

        changeRunningStatusTo("Write data to local tmp file...");

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withFields(analysisObject)
                .withPageable(PageRequest.of(0, 1000))
                .build();
        SearchScrollHits<Paper> hits = template.searchScrollStart(3000, searchQuery, Paper.class, IndexCoordinates.of("paper"));
        String scrollId = hits.getScrollId();
        do {
            hits.forEach(hit -> {
                List<String> items;
                if (analysisObject.equals("topics"))
                    items = hit.getContent().getTopics();
                else
                    items = hit.getContent().getSubjects();
                String inputData = StringUtils.join(FPGMainClass.splitChar, items) + "\n";
                try {
                    fileWriter.write(inputData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            hits = template.searchScrollContinue(scrollId, 3000, Paper.class, IndexCoordinates.of("paper"));
        } while (hits.hasSearchHits());
        fileWriter.close();

        changeRunningStatusTo("Data is ready!");
    }

    private Job frequencyCal() throws IOException, InterruptedException, ClassNotFoundException {
        changeRunningStatusTo("Counting frequency...");

        Job countJob = Job.getInstance(configuration, "Word Frequency");
        countJob.setJarByClass(FPGMainClass.class);

        countJob.setMapperClass(WordFrequentMapper.class);
        countJob.setCombinerClass(WordFrequentReducer.class);
        countJob.setReducerClass(WordFrequentReducer.class);

        countJob.setOutputKeyClass(Text.class);
        countJob.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(countJob, new Path(inputPath));
        FileOutputFormat.setOutputPath(countJob, new Path(countPath));

        if (!countJob.waitForCompletion(true)) {

            changeRunningStatusToStop("Frequency count error!");

            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }

        changeRunningStatusTo("Frequency count finished!");
        return countJob;
    }

    private void sortItem(Job countJob) throws IOException, InterruptedException, ClassNotFoundException {
        changeRunningStatusTo("Generating and sorting frequent items...");

        minSupport = minSupport * countJob.getCounters().findCounter(WordFrequency.Counter.LINE_LEN).getValue();
        countJob.close();
        configuration.setDouble("minSupport", minSupport);

        Job sortJob = Job.getInstance(configuration, "Sort");
        sortJob.setJarByClass(FPGMainClass.class);
        sortJob.setMapperClass(SortMapper.class);
        sortJob.setReducerClass(SortReducer.class);

        sortJob.setInputFormatClass(KeyValueTextInputFormat.class);
        sortJob.setMapOutputKeyClass(WordFrequency.class);
        sortJob.setMapOutputValueClass(IntWritable.class);

        sortJob.setOutputKeyClass(Text.class);
        sortJob.setOutputValueClass(IntWritable.class);
        FileInputFormat.setInputPaths(sortJob, new Path(countPath));
        FileOutputFormat.setOutputPath(sortJob, new Path(frequentItemsPath));

        if (!sortJob.waitForCompletion(true)) {
            changeRunningStatusToStop("Frequent items sort error!");

            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }

        changeRunningStatusTo("Frequent items sorting finished!");
    }

    private void fpgExc() throws IOException, InterruptedException, ClassNotFoundException {
        changeRunningStatusTo("FP-growth executing...");

        Job fpJob = Job.getInstance(configuration, "FPGrowth");
        fpJob.setJarByClass(FPGMainClass.class);
        fpJob.addCacheFile(new Path(frequentItemsPath + "/part-r-00000").toUri());

        fpJob.setMapperClass(FpgMapper.class);
        fpJob.setReducerClass(FpgReducer.class);

        fpJob.setMapOutputKeyClass(Text.class);
        fpJob.setMapOutputValueClass(Text.class);

        fpJob.setOutputKeyClass(NullWritable.class);
        fpJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(fpJob, new Path(inputPath));
        FileOutputFormat.setOutputPath(fpJob, new Path(frequentSetsPath));

        if (!fpJob.waitForCompletion(true)) {
            changeRunningStatusToStop("FP-Growth error!");

            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }
        fpJob.close();

        changeRunningStatusTo("FP-Growth finished!");
    }

    private void associationCal() throws IOException, InterruptedException, ClassNotFoundException {
        changeRunningStatusTo("Calculating association rules...");

        configuration.setDouble("confidence", minConfidence);

        Job rulesJob = Job.getInstance(configuration, "association rules job");
        rulesJob.setJarByClass(FPGMainClass.class);
        rulesJob.addCacheFile(new Path(frequentItemsPath + "/part-r-00000").toUri());
        FileInputFormat.addInputPath(rulesJob, new Path(frequentSetsPath));

        rulesJob.setMapperClass(AssociationMapper.class);
        rulesJob.setReducerClass(AssociationReducer.class);

        rulesJob.setMapOutputKeyClass(Text.class);
        rulesJob.setMapOutputValueClass(AssociationRule.class);

        rulesJob.setOutputKeyClass(NullWritable.class);
        rulesJob.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(rulesJob, new Path(associationRulesPath));

        if (!rulesJob.waitForCompletion(true)) {
            changeRunningStatusToStop("Association rules calculating error!");

            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }
        rulesJob.close();

        changeRunningStatusTo("Association rules calculating finished!");
    }

    private void deleteDir(String dirPath) throws IOException {
        File resultDir = new File(dirPath);
        if (resultDir.isDirectory()) {
            File[] sonDirs = resultDir.listFiles();
            assert sonDirs != null;
            for (File dir : sonDirs) {
                File[] tmpFiles = dir.listFiles();
                assert tmpFiles != null;
                for (File file : tmpFiles) {
                    if (!file.delete()) {
                        changeRunningStatusToStop("Can't delete tmp files");
                        throw new IOException(analysisObject + " analysis: Can't delete tmp files");
                    }
                }
                if (!dir.delete()) {
                    changeRunningStatusToStop("Can't delete tmp files");
                    throw new IOException(analysisObject + " analysis: Can't delete tmp files");
                }
            }
        }
        if(!resultDir.delete()) {
            changeRunningStatusToStop("Can't delete files");
            throw new IOException(analysisObject + " analysis: Can't delete files");
        }
        System.out.println(analysisObject + " analysis: Delete tmp files at " + dirPath);
    }
}
