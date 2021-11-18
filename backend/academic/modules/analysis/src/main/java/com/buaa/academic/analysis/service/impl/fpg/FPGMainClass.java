package com.buaa.academic.analysis.service.impl.fpg;

import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationMapper;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationReducer;
import com.buaa.academic.analysis.service.impl.fpg.FPGrowth.FpgMapper;
import com.buaa.academic.analysis.service.impl.fpg.FPGrowth.FpgReducer;
import com.buaa.academic.analysis.service.impl.fpg.FilterAndSort.SortMapper;
import com.buaa.academic.analysis.service.impl.fpg.FilterAndSort.SortReducer;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequency;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequentMapper;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequentReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FPGMainClass {
    //todo 线程化
    public static String splitChar = " ";
    private static boolean deleteTmpFiles = false;

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("FP-Growth analysis starting...");

        String inputPath = "./modules/analysis/src/main/resources/data/data.txt";
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String resultDir = "./modules/analysis/src/main/resources/fpgResult" +time;
        String countPath =  resultDir +"\\count";
        String frequentItemsPath = resultDir + "\\frequentItems";
        String frequentSetsPath =  resultDir + "\\frequentSets";
        String associationRulesPath = resultDir + "\\associationRules";

        Configuration configuration = new Configuration(true);

        //最小支持度
        double minSupport = 0.4;

        // 词频统计
        System.out.println("Counting frequency...");
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
            System.out.println("frequency count error!");
            System.exit(-1);
        }
        System.out.println("Frequency count finished!");

        // 去除非频繁项，根据词频对原数据排序
        System.out.println("Generating and sorting frequent items...");
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
            System.out.println("Frequent items sort error!");
            System.exit(-1);
        }
        System.out.println("Frequent items sorting finished!");


        // fp-growth生成频繁项集
        System.out.println("FP-growth executing...");
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
            System.out.println("FP-Growth error!");
            System.exit(-1);
        }
        fpJob.close();
        System.out.println("FP-Growth finished!");

        /*
        // 计算关联规则
        System.out.println("Calculating association rules...");
        double confidence = 0.6;
        configuration.setDouble("confidence", confidence);
        Job rulesJob = Job.getInstance(configuration, "association rules job");
        rulesJob.setJarByClass(FPGMainClass.class);
        rulesJob.addCacheFile(new Path(frequentItemsPath + "/part-r-00000").toUri());

        rulesJob.setMapperClass(AssociationMapper.class);
        rulesJob.setReducerClass(AssociationReducer.class);
        rulesJob.setMapOutputKeyClass(Text.class);
        rulesJob.setMapOutputValueClass(Text.class);
        rulesJob.setOutputKeyClass(Text.class);
        rulesJob.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(rulesJob, new Path(frequentSetsPath));
        FileOutputFormat.setOutputPath(rulesJob, new Path(associationRulesPath));

        if (!rulesJob.waitForCompletion(true)) {
            System.out.println("Association rules calculate error!");
            System.exit(-1);
        }
        rulesJob.close();
        System.out.println("Association rules calculate finished!");
           */
        //todo 接入es

        if (FPGMainClass.deleteTmpFiles) {
            deleteDir(resultDir);
            System.out.println("Tmp files delete");
        }
        System.out.println("All Down!");
    }

    private static void deleteDir(String dirPath) throws IOException {
        File resultDir = new File(dirPath);
        File[] sonDirs = resultDir.listFiles();
        assert sonDirs != null;
        for (File dir : sonDirs) {
            File[] tmpFiles = dir.listFiles();
            assert tmpFiles != null;
            for (File file: tmpFiles) {
                if (!file.delete()) {
                    throw new IOException("Can't delete tmp files");
                }
            }
            if(!dir.delete())
                throw new IOException("Can't delete tmp files");
        }
        if(!resultDir.delete())
            throw new IOException("Can't delete files");
    }
}
