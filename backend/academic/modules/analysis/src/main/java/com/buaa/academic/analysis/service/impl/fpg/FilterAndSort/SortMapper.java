package com.buaa.academic.analysis.service.impl.fpg.FilterAndSort;

import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequency;
import com.buaa.academic.analysis.service.impl.fpg.StatusCtrl;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SortMapper extends
        Mapper<Text, Text, WordFrequency, IntWritable> {
    private double support;

    @Override
    protected void setup(
            Mapper<Text, Text, WordFrequency, IntWritable>.Context context)
            throws IOException, InterruptedException {
        support = context.getConfiguration().getDouble("minSupport", 0);

        if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
            StatusCtrl.stop(context.getJobName());
        }
    }

    @Override
    protected void map(Text key, Text value,Context context)
            throws IOException, InterruptedException {

        if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
            StatusCtrl.stop(context.getJobName());
        }

        int val = Integer.parseInt(value.toString());
        if(val >= support)
            context.write(new WordFrequency(key.toString(), val), new IntWritable(1));
    }
}
