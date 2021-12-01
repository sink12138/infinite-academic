package com.buaa.academic.analysis.service.impl.fpg.FilterAndSort;

import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.FpgWordFrequency;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SortReducer extends
        Reducer<FpgWordFrequency, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(FpgWordFrequency key, Iterable<IntWritable> values,
                          Reducer<FpgWordFrequency, IntWritable, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
            StatusCtrl.stop(context.getJobName());
        }
        context.write(new Text(key.getWord() + ":"), new IntWritable(key.getFrequency()));
    }
}
