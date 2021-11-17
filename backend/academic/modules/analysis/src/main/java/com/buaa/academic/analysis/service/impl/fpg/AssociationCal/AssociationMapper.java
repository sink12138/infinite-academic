package com.buaa.academic.analysis.service.impl.fpg.AssociationCal;

import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.fpg.FPTree.FPTreeNode;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.KeyValueLineRecordReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AssociationMapper extends Mapper<LongWritable, Text, Text, Text> {
    private Map<String, Integer> supportMap;

    @Override
    protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context) throws IOException {
        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0)
        {
            supportMap = new HashMap<>();
            FileSystem fs = FileSystem.get(cacheFiles[0], context.getConfiguration());

            BufferedReader joinReader = new BufferedReader(new InputStreamReader(fs.open(new Path(cacheFiles[0]))));
            String line;

            joinReader.close();
        }
    }
}
