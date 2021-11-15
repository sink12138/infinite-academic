package com.buaa.academic.analysis.service.impl.fpg;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class AnalysisMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
    @Override
    protected void map(LongWritable key, Text value,
                       Mapper<LongWritable, Text, Text, LongWritable>.Context context)
            throws IOException, InterruptedException {
        String[] words = value.toString().split(" ");
        for (String word : words) {
            context.write(new Text(word), new LongWritable(1));
        }
    }
}
