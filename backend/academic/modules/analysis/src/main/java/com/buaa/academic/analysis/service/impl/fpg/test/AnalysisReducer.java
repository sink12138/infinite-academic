package com.buaa.academic.analysis.service.impl.fpg.test;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class AnalysisReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

    @Override
    protected void reduce(Text key, Iterable<LongWritable> values,
                          Reducer<Text, LongWritable, Text, LongWritable>.Context context) throws InterruptedException, IOException {
        long sum = 0;
        for (LongWritable i : values) {
            // i.get转换成long类型
            sum += i.get();
        }
        // 输出总计结果
        context.write(key, new LongWritable(sum));
    }

}
