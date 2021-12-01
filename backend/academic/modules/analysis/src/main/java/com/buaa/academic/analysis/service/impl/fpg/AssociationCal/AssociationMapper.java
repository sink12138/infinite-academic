package com.buaa.academic.analysis.service.impl.fpg.AssociationCal;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AssociationMapper extends Mapper<LongWritable, Text, Text, AssociationRule> {
    private Map<String, Integer> supportMap;
    private double confidence;

    @Override
    protected void setup(Mapper<LongWritable, Text, Text, AssociationRule>.Context context) throws IOException {
        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0)
        {
            confidence = context.getConfiguration().getDouble("confidence", 0);
            supportMap = new HashMap<>();
            FileSystem fs = FileSystem.get(cacheFiles[0], context.getConfiguration());

            BufferedReader joinReader = new BufferedReader(new InputStreamReader(fs.open(new Path(cacheFiles[0]))));
            String line;
            try{
                while ((line = joinReader.readLine()) != null) {

                    if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                        StatusCtrl.stop(context.getJobName());
                    }

                    String[] itemFreq = line.split(":");//读出频繁一项集，以TreeNode类型保存到集合中
                    supportMap.put(itemFreq[0], Integer.parseInt(itemFreq[1].split("\\s+")[1]));
                }
            }catch (IOException e) {
                System.err.println("Exception reading DistributedCache: " + e);
            }
            joinReader.close();
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().trim();
        String[] freSet = line.split(":");
        String[] freItems = freSet[0].split(FPGMainClass.splitChar);
        int frequencyOfSet = Integer.parseInt(freSet[1]);
        for (String item : freItems) {

            if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                StatusCtrl.stop(context.getJobName());
            }

            int frequencyOfItem = supportMap.get(item);
            double selfConfidence = ((double) frequencyOfSet) / ((double) frequencyOfItem);
            if (selfConfidence > confidence) {
                context.write(new Text(item), new AssociationRule(item, freSet[0], selfConfidence));
            }
        }
    }
}
