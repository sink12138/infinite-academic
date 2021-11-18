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
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FPGMainClass implements Runnable{

    public static String splitChar = " ";
    private final String analysisObject;
    private final String inputPath;
    private double minSupport;
    private final double minConfidence;
    private final boolean deleteTmpFiles;
    private final String resultDir;
    private final String countPath;
    private final String frequentItemsPath;
    private final String frequentSetsPath;
    private final String associationRulesPath;
    private final Configuration configuration;

    public FPGMainClass(double minSupport, double minConfidence, boolean deleteTmpFiles, String analysisObject) {
        this.inputPath = "./modules/analysis/src/main/resources/" + analysisObject;
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.deleteTmpFiles = deleteTmpFiles;
        this.analysisObject = analysisObject;
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        resultDir = "./modules/analysis/src/main/resources/fpgResult" + time;
        countPath =  resultDir +"\\count";
        frequentItemsPath = resultDir + "\\frequentItems";
        frequentSetsPath =  resultDir + "\\frequentSets";
        associationRulesPath = resultDir + "\\associationRules";
        configuration = new Configuration(true);
    }

    @SneakyThrows
    @Override
    public void run() {
        System.out.println(analysisObject + " analysis: FP-Growth analysis starting...");
        long start_time = System.currentTimeMillis();

        getInputData();

        /*
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
        }*/
        System.out.println(analysisObject + " analysis: All Down!");
        System.out.println(analysisObject + " analysis: Cost " + ((double)(System.currentTimeMillis() - start_time) / 1000) + "s");
    }

    @Autowired
    private ElasticsearchRestTemplate template;

    private void getInputData() throws IOException {
        File inputFile = new File(inputPath);
        if (!inputFile.createNewFile()) {
            System.out.println("Can't make file " + inputFile);
            throw new IOException();
        }
        FileWriter fileWriter = new FileWriter(inputFile);

        System.out.println(analysisObject + " analysis: Write data to local tmp file..");
        int pageContent = 10000;
        int page = 1;
        int totalPage = -1;
        do {
            SearchHits<Paper> hits = template.search(
                    new NativeSearchQueryBuilder()
                            .withPageable(PageRequest.of(page, pageContent))
                            .build(),
                    Paper.class
            );
            page ++;

            if (totalPage < 0)
                totalPage = (int) (hits.getTotalHits() + (pageContent - 1)) / pageContent;

            hits.forEach(hit -> {
                List<String> items;
                if (analysisObject.equals("topic"))
                    items = hit.getContent().getTopics();
                else
                    items = hit.getContent().getSubjects();
                String inputData = StringUtils.join(items, FPGMainClass.splitChar) + "\n";
                try {
                    fileWriter.write(inputData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } while (page <= totalPage);
        fileWriter.close();
        System.out.println(analysisObject + " analysis: Data is ready!");
    }

    private Job frequencyCal() throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println(analysisObject + " analysis: Counting frequency...");
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
            System.out.println(analysisObject + " analysis: frequency count error!");
            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }
        System.out.println(analysisObject + " analysis: Frequency count finished!");
        return countJob;
    }

    private void sortItem(Job countJob) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println(analysisObject + " analysis: Generating and sorting frequent items...");
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
            System.out.println(analysisObject + " analysis: Frequent items sort error!");
            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }
        System.out.println(analysisObject + " analysis: Frequent items sorting finished!");
    }

    private void fpgExc() throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println(analysisObject + " analysis: FP-growth executing...");
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
            System.out.println(analysisObject + " analysis: FP-Growth error!");
            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }
        fpJob.close();
        System.out.println(analysisObject + " analysis: FP-Growth finished!");
    }

    private void associationCal() throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println(analysisObject + " analysis: Calculating association rules...");
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
            System.out.println(analysisObject + " analysis: Association rules calculating error!");
            deleteDir(resultDir);
            deleteDir(inputPath);
            System.exit(-1);
        }
        rulesJob.close();
        System.out.println(analysisObject + " analysis: Association rules calculating finished!");
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
                        throw new IOException(analysisObject + " analysis: Can't delete tmp files");
                    }
                }
                if (!dir.delete())
                    throw new IOException(analysisObject + " analysis: Can't delete tmp files");
            }
        }
        if(!resultDir.delete())
            throw new IOException(analysisObject + " analysis: Can't delete files");
        System.out.println(analysisObject + " analysis: Delete tmp files at " + dirPath);
    }
}
