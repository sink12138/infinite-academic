package com.buaa.academic.analysis.service.impl.fpg.FrequencyCount;

import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordFrequentReducer extends
        Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for(IntWritable val: values) {

            if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                StatusCtrl.stop(context.getJobName());
            }

            sum += val.get();
        }
        context.write(key, new IntWritable(sum));
    }
}
