package com.buaa.academic.analysis.service.impl.fpg;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.stream.Stream;


public class testMapReduce {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, URISyntaxException {
        /*
        Configuration configuration = new Configuration();
        String hdfsUri = "hdfs://localhost:9300";
        configuration.set("fs.defaultFS", hdfsUri);
        URI uri = new URI(hdfsUri.trim());
        FileSystem fileSystem = FileSystem.get(uri, configuration);
        //fileSystem.mkdirs(new Path("modules/analysis/src/main/resources/result/"));
        */
        Job job = Job.getInstance(new Configuration());
        job.setJarByClass(testMapReduce.class);
        job.setMapperClass(AnalysisMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        String dataPath = "modules/analysis/src/main/resources/data/test.txt";
        FileInputFormat.setInputPaths(job, new Path(dataPath));

        job.setReducerClass(AnalysisReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        String resultPath = "modules/analysis/src/main/resources/result/";
        FileOutputFormat.setOutputPath(job, new Path(resultPath));
        job.waitForCompletion(true);
        System.out.println("Finished");
    }
}
