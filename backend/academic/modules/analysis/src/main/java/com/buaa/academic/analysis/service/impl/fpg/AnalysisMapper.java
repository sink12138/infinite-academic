package com.buaa.academic.analysis.service.impl.fpg;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.elasticsearch.common.logging.ESJsonLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnalysisMapper extends Mapper<LongWritable, Text, Text, MapWritable> {
    @Override
    protected void map(LongWritable key, Text value,
                       Mapper<LongWritable, Text, Text, MapWritable>.Context context)
            throws IOException, InterruptedException {
        String[] words = value.toString().split(" ");
        for (String word : words) {
            MapWritable mapWritable = new MapWritable();
            LongWritable[] longWritableNumbers = new LongWritable[100];
            longWritableNumbers[1] = new LongWritable(1);
            ArrayWritable sons = new ArrayWritable(LongWritable.class, longWritableNumbers);
            context.write(new Text(word), mapWritable);
        }
    }
}
