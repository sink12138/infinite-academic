package com.buaa.academic.analysis.service.impl.fpg.FPGrowth;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.fpg.FPTree.FPTree;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FpgReducer extends Reducer<Text, Text, NullWritable, Text> {
    private double support;

    @Override
    protected void setup(Reducer<Text, Text, NullWritable, Text>.Context context) {
        support = context.getConfiguration().getDouble("minSupport", 0);
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values,
                          Reducer<Text, Text, NullWritable, Text>.Context context)
            throws IOException, InterruptedException {
        ArrayList<ArrayList<String>> transactions = new ArrayList<>();
        int sup = 0;
        for(Text val: values) {

            if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                StatusCtrl.stop(context.getJobName());
            }

            ArrayList<String> items = new ArrayList<>(Arrays.asList(val.toString().split(FPGMainClass.splitChar)));
            transactions.add(items);									//将传递的条件模式基拆分后保存到集合
            sup++;
        }
        if (sup > support) {
            FPTree fpTree = new FPTree(support);
            ArrayList<String> freqSet = fpTree.fp_growth(transactions, key.toString());
            for (String freqItem: freqSet) {

                if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                    StatusCtrl.stop(context.getJobName());
                }

                context.write(NullWritable.get(), new Text(freqItem));
            }
        }
   }
}
