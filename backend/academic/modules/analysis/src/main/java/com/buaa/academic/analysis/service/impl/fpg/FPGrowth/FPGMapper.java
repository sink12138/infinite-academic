package com.buaa.academic.analysis.service.impl.fpg.FPGrowth;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.fpg.FPTree.FPTree;
import com.buaa.academic.analysis.service.impl.fpg.FPTree.FPTreeNode;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class FPGMapper extends Mapper<LongWritable, Text, Text, Text> {

    private LinkedList<FPTreeNode> itemsSorted;
    private FPTree fpTree;

    @Override
    protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
        throws IOException {
        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0)
        {
            fpTree = new FPTree();											//初始化
            itemsSorted = new LinkedList<>();
            FileSystem fs = FileSystem.get(cacheFiles[0], context.getConfiguration());
            BufferedReader joinReader = new BufferedReader(new InputStreamReader(fs.open(new Path(cacheFiles[0]))));
            String line;
            try{
                while ((line = joinReader.readLine()) != null) {

                    if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                        StatusCtrl.stop(context.getJobName());
                    }

                    String[] itemFreq = line.split(":");//读出频繁一项集，以TreeNode类型保存到集合中
                    try {
                        itemsSorted.add(new FPTreeNode(itemFreq[0], Integer.parseInt(itemFreq[1].split("\\s+")[1])));
                    } catch (Exception e) {
                        System.out.println(line);
                    }
                }
            }catch (IOException e) {
                System.err.println("Exception reading DistributedCache: " + e);
            }
            joinReader.close();
        }
    }

    @Override
    protected void map(LongWritable key, Text value,
                       Mapper<LongWritable, Text, Text, Text>.Context context)
            throws IOException, InterruptedException {
        String line = value.toString().trim();
        ArrayList<String> appearedItemsList = new ArrayList<>(Arrays.asList(line.split(FPGMainClass.splitChar)));
        ArrayList<ArrayList<String>> trans = new ArrayList<>();
        trans.add(appearedItemsList);
        ArrayList<String> appearedItemsSorted = fpTree.sortByFreqItem(trans, itemsSorted).get(0);

        String k, prefix;
        for(int i = appearedItemsSorted.size() - 1; i > 0; i--){

            if (StatusCtrl.isStopped(context.getConfiguration().get("name"))) {
                StatusCtrl.stop(context.getJobName());
            }

            k = appearedItemsSorted.get(i);								//从后往前遍历排好序的数据，每次取出当前支持度最小的项
            appearedItemsSorted.remove(appearedItemsSorted.size() - 1);
            prefix = StringUtils.join(FPGMainClass.splitChar, appearedItemsSorted);
            context.write(new Text(k), new Text(prefix));				//将该项前的所有项作为该项的条件模式基，传递给Reducer
        }
    }
}
