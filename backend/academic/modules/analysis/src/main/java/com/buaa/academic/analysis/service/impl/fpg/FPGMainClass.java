package com.buaa.academic.analysis.service.impl.fpg;

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
    private final String analysisObject; // 用于区分对话题还是学科进行分析
    private String name; // 用于获取当前作业的运行状态
    private final String inputPath;
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

    public FPGMainClass(String analysisObject) {
        this.analysisObject = analysisObject;
        this.inputPath = "./modules/analysis/src/main/resources/" + analysisObject + "Data";
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        resultDir = "./modules/analysis/src/main/resources/fpgResult_" + analysisObject + time;
        countPath =  resultDir +"\\count";
        frequentItemsPath = resultDir + "\\frequentItems";
        frequentSetsPath =  resultDir + "\\frequentSets";
        associationRulesPath = resultDir + "\\associationRules";
        configuration = new Configuration(true);
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
        configuration.set("name", name);
        this.name = name;
        return this;
    }

    @SneakyThrows
    @Override
    public void run() {

        changeRunningStatusTo("FP-Growth analysis starting...");

        long start_time = System.currentTimeMillis();

        try {
            getInputData();
        } catch (Exception e) {
            interruptStop(e.toString());
        }
        if(threadStopCheck())
            return;

        // 词频统计
        Job countJob = null;
        try {
            countJob = frequencyCal();
        } catch (IllegalStateException e) {
            interruptStop("Stopped");
        } catch (Exception e) {
            interruptStop(e.toString());
        }
        if(threadStopCheck())
            return;

        // 去除非频繁项，根据词频对原数据排序
        try {
            assert countJob != null;
            sortItem(countJob);
        } catch (IllegalStateException e) {
            interruptStop("Stopped");
        } catch (Exception e) {
            interruptStop(e.toString());
        }
        if(threadStopCheck())
            return;

        // fp-growth生成频繁项集
        try {
            fpgExc();
        }catch (IllegalStateException e) {
            interruptStop("Stopped");
        } catch (Exception e) {
            interruptStop(e.toString());
        }
        if(threadStopCheck())
            return;

        // 计算关联规则
        try {
            associationCal();
        }catch (IllegalStateException e) {
            interruptStop("Stopped");
        } catch (Exception e) {
            interruptStop(e.toString());
        }

        if (deleteTmpFiles) {
            deleteDir(resultDir);
            deleteDir(inputPath);
        }

        double costTime = ((double)(System.currentTimeMillis() - start_time) / 1000);
        changeRunningStatusToStop("All Down! " + "Cost " + costTime + "s");
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

            // 检查线程是否终止
            if (StatusCtrl.isStopped(Thread.currentThread().getName())) {
                changeRunningStatusToStop("Stopped.");
                fileWriter.close();
                return;
            }

            // 写入
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

        String jobName = "Word Frequency";
        Job countJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, countJob);

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

        String jobName = "Sort";
        Job sortJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, sortJob);

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

        String jobName = "FPGrowth";
        Job fpJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, fpJob);

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

        String jobName = "association rules job";
        Job rulesJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, rulesJob);

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

    private void changeRunningStatusTo(String runningStatus) {
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.runningStatus.put(name, runningStatus);
        }
        System.out.println(name + ": " + runningStatus);
    }

    private void changeRunningStatusToStop(String runningStatus) {
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.remove(name);
            StatusCtrl.runningStatus.put(name, runningStatus);
        }
        System.out.println(name + ": " + runningStatus);
    }

    private void deleteDir(String dirPath) throws IOException {
        File resultDir = new File(dirPath);
        if (resultDir.exists()) {
            if (resultDir.isDirectory()) {
                File[] sonDirs = resultDir.listFiles();
                assert sonDirs != null;
                for (File dir : sonDirs) {
                    File[] tmpFiles = dir.listFiles();
                    assert tmpFiles != null;
                    for (File file : tmpFiles) {
                        if (!file.delete()) {
                            changeRunningStatusToStop("Can't delete tmp files at" + file.getPath());
                            throw new IOException(analysisObject + " analysis: Can't delete tmp files at" + file.getPath());
                        }
                    }
                    if (!dir.delete()) {
                        changeRunningStatusToStop("Can't delete tmp files" + dir.getPath());
                        throw new IOException(analysisObject + " analysis: Can't delete tmp files at" + dir.getPath());
                    }
                }
            }
            if (!resultDir.delete()) {
                changeRunningStatusToStop("Can't delete files at" + resultDir.getPath());
                throw new IOException(analysisObject + " analysis: Can't delete files at" + resultDir.getPath());
            }
            System.out.println(analysisObject + " analysis: Delete tmp files at " + dirPath);
        }
    }

    private boolean threadStopCheck() throws IOException {
        if (StatusCtrl.isStopped(Thread.currentThread().getName())) {
            interruptStop("Stopped");
            return true;
        }
        return false;
    }

    private void interruptStop(String reason) throws IOException {
        deleteDir(inputPath);
        deleteDir(resultDir);
        changeRunningStatusToStop(reason);
    }
}
