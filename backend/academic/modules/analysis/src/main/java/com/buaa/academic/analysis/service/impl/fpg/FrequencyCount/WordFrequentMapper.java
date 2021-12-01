package com.buaa.academic.analysis.service.impl.fpg.FrequencyCount;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class WordFrequentMapper
        extends Mapper<LongWritable, Text, Text, IntWritable> {
    @Override
    protected void map(LongWritable key, Text value,Context context)
            throws IOException, InterruptedException {
        String line = value.toString();
        String[] items = line.trim().split(FPGMainClass.splitChar);
        for(String tmp:items) {

            if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                StatusCtrl.stop(context.getJobName());
            }

            context.write(new Text(tmp), new IntWritable(1));
        }
        context.getCounter(FpgWordFrequency.Counter.LINE_LEN).increment(1);		//每处理一行数据，计数，LINE_LEN保存总事务数
    }
}
