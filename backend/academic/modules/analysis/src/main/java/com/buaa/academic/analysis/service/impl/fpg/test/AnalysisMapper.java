package com.buaa.academic.analysis.service.impl.fpg.test;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.elasticsearch.common.logging.ESJsonLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
