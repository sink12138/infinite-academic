package com.buaa.academic.analysis.service.impl.fpg.FilterAndSort;

import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequency;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SortReducer extends
        Reducer<WordFrequency, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(WordFrequency key, Iterable<IntWritable> values,
                          Reducer<WordFrequency, IntWritable, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
            StatusCtrl.stop(context.getJobName());
        }
        context.write(new Text(key.getWord()), new IntWritable(key.getFrequency()));
    }
}
