package com.buaa.academic.analysis.service.impl.fpg.AssociationCal;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

public class AssociationReducer extends Reducer<Text, AssociationRule, NullWritable, Text> {
    @Override
    protected void reduce(Text key, Iterable<AssociationRule> values, Reducer<Text, AssociationRule, NullWritable, Text>.Context context)
            throws IOException, InterruptedException {
        TreeSet<AssociationRule> associationRules = new TreeSet<>();
        values.forEach(val ->
                associationRules.add(new AssociationRule(val.getItem(), val.getAssociationItems(), val.getConfidence())));
        ArrayList<String> associationList = new ArrayList<>();
        String item = key.toString();
        for (AssociationRule  rule : associationRules) {
            String[] items = rule.getAssociationItems().split(FPGMainClass.splitChar);
            for (String associationItem : items) {
                if (!associationList.contains(associationItem) && !item.equals(associationItem)) {
                    associationList.add(associationItem);
                }
            }
        }
        String jsonRes = "{\"item\": \"" + item + "\", \"associationSet\": \"" + StringUtils.join(associationList, FPGMainClass.splitChar) + "\"}";
        context.write(NullWritable.get(), new Text(jsonRes));
    }
}
