package com.buaa.academic.analysis.service.impl.fpg;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationMapper;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationReducer;
import com.buaa.academic.analysis.service.impl.fpg.AssociationCal.AssociationRule;
import com.buaa.academic.analysis.service.impl.fpg.FPGrowth.FPGMapper;
import com.buaa.academic.analysis.service.impl.fpg.FPGrowth.FPGReducer;
import com.buaa.academic.analysis.service.impl.fpg.FilterAndSort.SortMapper;
import com.buaa.academic.analysis.service.impl.fpg.FilterAndSort.SortReducer;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.FPGWordFrequency;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequentMapper;
import com.buaa.academic.analysis.service.impl.fpg.FrequencyCount.WordFrequentReducer;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.statistic.Association;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public class FPGMainClass implements Runnable {

    public static String splitChar = ",";
    private final String analysisObject; // 用于区分对话题还是学科进行分析
    private String name; // 用于获取当前作业的运行状态
    private String inputPath;
    private double minSupport;
    private double minConfidence;
    private boolean deleteTmpFiles;
    private String resultDir;
    private String countPath;
    private String frequentItemsPath;
    private String frequentSetsPath;
    private String associationRulesPath;
    private ElasticsearchRestTemplate template;
    private final Configuration configuration;

    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;

    public FPGMainClass(String analysisObject) {
        this.analysisObject = analysisObject;
        configuration = new Configuration(true);
    }

    public FPGMainClass setMinSupport(double minSupport) {
        this.minSupport = minSupport;
        return this;
    }

    public FPGMainClass setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
        return this;
    }

    public FPGMainClass setDeleteTmpFiles(boolean deleteTmpFiles) {
        this.deleteTmpFiles = deleteTmpFiles;
        return this;
    }

    public FPGMainClass setTemplate(ElasticsearchRestTemplate template) {
        this.template = template;
        return this;
    }

    public FPGMainClass setCacheDirectory(String directory) {
        File cache = new File(directory);
        if (!cache.exists() && !cache.mkdirs())
            throw new RuntimeException("Cannot create cache directory");
        this.inputPath = new File(directory, analysisObject + "-data").getPath();
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        this.resultDir = new File(directory, "FPGResult-" + analysisObject + '-' + time).getPath();
        this.countPath = new File(this.resultDir, "count").getPath();
        this.frequentItemsPath = new File(this.resultDir, "frequentItems").getPath();
        this.frequentSetsPath = new File(this.resultDir, "frequentSets").getPath();
        this.associationRulesPath = new File(resultDir, "associationRules").getPath();
        return this;
    }

    public FPGMainClass setName(String name) {
        configuration.set("name", name);
        this.name = name;
        return this;
    }

    public FPGMainClass setTopicRepository(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        return this;
    }

    public FPGMainClass setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
        return this;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("FP-Growth started");

        StatusCtrl.changeRunningStatusTo("FP-Growth analysis starting...", name);

        long startTime = System.currentTimeMillis();

        try {
            getInputData();
        } catch (Exception e) {
            e.printStackTrace();
            interruptStop(e.toString());
        }
        if (threadStopCheck(name))
            return;

        // 词频统计
        Job countJob = null;
        try {
            countJob = frequencyCal();
            if (countJob == null)
                return;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            interruptStop("Stopped. ");
        } catch (Exception e) {
            e.printStackTrace();
            interruptStop(e.toString());
        }
        if (threadStopCheck(name))
            return;

        // 去除非频繁项，根据词频对原数据排序
        try {
            assert countJob != null;
            if (!sortItem(countJob))
                return;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            interruptStop("Stopped. ");
        } catch (Exception e) {
            e.printStackTrace();
            interruptStop(e.toString());
        }
        if (threadStopCheck(name))
            return;

        // fp-growth生成频繁项集
        try {
            if(!fpgExc())
                return;
        } catch (IllegalStateException e) {
            interruptStop("Stopped. ");
        } catch (Exception e) {
            e.printStackTrace();
            interruptStop(e.toString());
        }
        if (threadStopCheck(name))
            return;

        // 计算关联规则
        try {
            if (!associationCal())
                return;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            interruptStop("Stopped. ");
        } catch (Exception e) {
            e.printStackTrace();
            interruptStop(e.toString());
        }
        if (threadStopCheck(name))
            return;

        try {
            saveToEs();
        } catch (Exception e) {
            e.printStackTrace();
            interruptStop(e.toString());
        }

        StatusCtrl.changeRunningStatusTo("Deleting tmp files... ", name);
        if (deleteTmpFiles) {
            deleteDir(resultDir);
            deleteDir(inputPath);
        }

        double costTime = ((double) (System.currentTimeMillis() - startTime) / 1000);
        StatusCtrl.changeRunningStatusToStop("All done! " + "Cost " + costTime + "s. ", name);

        log.info("FP-Growth finished");
    }

    private void getInputData() throws IOException {
        File inputFile = new File(inputPath);
        if (inputFile.exists()) {
            deleteDir(inputPath);
        }
        if (!inputFile.createNewFile()) {
            StatusCtrl.changeRunningStatusToStop("Can't make file " + inputFile, name);
            throw new IOException();
        }

        FileWriter fileWriter = new FileWriter(inputFile);

        StatusCtrl.changeRunningStatusTo("Write data to local tmp file...", name);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.existsQuery(analysisObject))
                .withFields(analysisObject)
                .withPageable(PageRequest.of(0, 1000))
                .build();
        SearchScrollHits<Paper> hits = template.searchScrollStart(120000, searchQuery, Paper.class, IndexCoordinates.of("paper"));
        String scrollId = hits.getScrollId();

        int total = 0;
        do {
            total += hits.getSearchHits().size();
            log.info("batch size: {}", total);

            // 检查线程是否终止
            if (StatusCtrl.isStopped(name)) {
                StatusCtrl.changeRunningStatusToStop("Stopped. ", name);
                fileWriter.close();
                return;
            }

            List<SearchHit<Paper>> searchHits = hits.getSearchHits();

            // 写入
            for (SearchHit<Paper> hit : searchHits) {
                List<String> items;
                if (analysisObject.equals("keywords")) {
                    items = hit.getContent().getKeywords();
                    if (items != null) {
                        for (String item : items) {
                            NativeSearchQuery query = new NativeSearchQueryBuilder()
                                    .withQuery(QueryBuilders.boolQuery().must(
                                            QueryBuilders.termQuery("name.raw", item)))
                                    .build();
                            SearchHits<Topic> topicHits = template.search(query, Topic.class);
                            if (!topicHits.hasSearchHits()) {
                                Topic topic = new Topic();
                                topic.setName(item);
                                topicRepository.save(topic);
                            }
                        }
                    }
                } else {
                    items = hit.getContent().getSubjects();
                    if (items != null) {
                        for (String item : items) {
                            NativeSearchQuery query = new NativeSearchQueryBuilder()
                                    .withQuery(QueryBuilders.boolQuery().must(
                                            QueryBuilders.termQuery("name.raw", item)))
                                    .build();
                            SearchHits<Subject> subjectHits = template.search(query, Subject.class);
                            if (!subjectHits.hasSearchHits()) {
                                Subject subject = new Subject();
                                subject.setName(item);
                                subjectRepository.save(subject);
                            }
                        }
                    }
                }
                if (items != null) {
                    String inputData = StringUtils.join(FPGMainClass.splitChar, items) + "\n";
                    fileWriter.write(inputData);
                }
            }
            hits = template.searchScrollContinue(scrollId, 120000, Paper.class, IndexCoordinates.of("paper"));
        } while (hits.hasSearchHits());
        StatusCtrl.changeRunningStatusTo("Data is ready!", name);
        fileWriter.close();
    }

    private Job frequencyCal() throws IOException, InterruptedException, ClassNotFoundException {
        StatusCtrl.changeRunningStatusTo("Counting frequency...", name);

        String jobName = "Word Frequency";
        Job countJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, countJob);

        countJob.setJarByClass(FPGMainClass.class);

        countJob.setMapperClass(WordFrequentMapper.class);
        countJob.setCombinerClass(WordFrequentReducer.class);
        countJob.setReducerClass(WordFrequentReducer.class);

        countJob.setOutputKeyClass(Text.class);
        countJob.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(countJob, new Path(inputPath));
        FileOutputFormat.setOutputPath(countJob, new Path(countPath));

        if (!countJob.waitForCompletion(true)) {

            StatusCtrl.changeRunningStatusToStop("Frequency count error!", name);

            deleteDir(resultDir);
            deleteDir(inputPath);
            return null;
        }

        StatusCtrl.changeRunningStatusTo("Frequency count finished!", name);
        return countJob;
    }

    private boolean sortItem(Job countJob) throws IOException, InterruptedException, ClassNotFoundException {
        StatusCtrl.changeRunningStatusTo("Generating and sorting frequent items...", name);

        minSupport = minSupport * countJob.getCounters().findCounter(FPGWordFrequency.Counter.LINE_LEN).getValue();
        countJob.close();
        configuration.setDouble("minSupport", minSupport);

        String jobName = "Sort";
        Job sortJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, sortJob);

        sortJob.setJarByClass(FPGMainClass.class);
        sortJob.setMapperClass(SortMapper.class);
        sortJob.setReducerClass(SortReducer.class);

        sortJob.setInputFormatClass(KeyValueTextInputFormat.class);
        sortJob.setMapOutputKeyClass(FPGWordFrequency.class);
        sortJob.setMapOutputValueClass(IntWritable.class);

        sortJob.setOutputKeyClass(Text.class);
        sortJob.setOutputValueClass(IntWritable.class);
        FileInputFormat.setInputPaths(sortJob, new Path(countPath));
        FileOutputFormat.setOutputPath(sortJob, new Path(frequentItemsPath));

        if (!sortJob.waitForCompletion(true)) {
            StatusCtrl.changeRunningStatusToStop("Frequent items sort error!", name);

            deleteDir(resultDir);
            deleteDir(inputPath);
            return false;
        }
        StatusCtrl.changeRunningStatusTo("Frequent items sorting finished!", name);
        return true;
    }

    private boolean fpgExc() throws IOException, InterruptedException, ClassNotFoundException {
        StatusCtrl.changeRunningStatusTo("FP-growth executing...", name);

        String jobName = "FPGrowth";
        Job fpJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, fpJob);

        fpJob.setJarByClass(FPGMainClass.class);
        fpJob.addCacheFile(new Path(frequentItemsPath + "/part-r-00000").toUri());

        fpJob.setMapperClass(FPGMapper.class);
        fpJob.setReducerClass(FPGReducer.class);

        fpJob.setMapOutputKeyClass(Text.class);
        fpJob.setMapOutputValueClass(Text.class);

        fpJob.setOutputKeyClass(NullWritable.class);
        fpJob.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(fpJob, new Path(inputPath));
        FileOutputFormat.setOutputPath(fpJob, new Path(frequentSetsPath));

        if (!fpJob.waitForCompletion(true)) {
            StatusCtrl.changeRunningStatusToStop("FP-Growth error!", name);

            deleteDir(resultDir);
            deleteDir(inputPath);
            return false;
        }
        fpJob.close();

        StatusCtrl.changeRunningStatusTo("FP-Growth finished!", name);
        return true;
    }

    private boolean associationCal() throws IOException, InterruptedException, ClassNotFoundException {
        StatusCtrl.changeRunningStatusTo("Calculating association rules...", name);

        configuration.setDouble("confidence", minConfidence);

        String jobName = "association rules job";
        Job rulesJob = Job.getInstance(configuration, jobName);
        StatusCtrl.currentJob.put(jobName, rulesJob);

        rulesJob.setJarByClass(FPGMainClass.class);
        rulesJob.addCacheFile(new Path(frequentItemsPath + "/part-r-00000").toUri());
        FileInputFormat.addInputPath(rulesJob, new Path(frequentSetsPath));

        rulesJob.setMapperClass(AssociationMapper.class);
        rulesJob.setReducerClass(AssociationReducer.class);

        rulesJob.setMapOutputKeyClass(Text.class);
        rulesJob.setMapOutputValueClass(AssociationRule.class);

        rulesJob.setOutputKeyClass(NullWritable.class);
        rulesJob.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(rulesJob, new Path(associationRulesPath));

        if (!rulesJob.waitForCompletion(true)) {
            StatusCtrl.changeRunningStatusToStop("Association rules calculating error!", name);

            deleteDir(resultDir);
            deleteDir(inputPath);
            return false;
        }
        rulesJob.close();

        StatusCtrl.changeRunningStatusTo("Association rules calculating finished!", name);
        return true;
    }

    private void saveToEs() throws IOException {
        StatusCtrl.changeRunningStatusTo("Write to ElasticSearch...", name);

        File resFile = new File(associationRulesPath + "/part-r-00000");
        FileReader fileReader = new FileReader(resFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] rule = line.split(":");
                String item = rule[0];
                String[] associationItems = rule[1].split(FPGMainClass.splitChar);
                List<String> confidencesStr = Arrays.asList(rule[2].split(FPGMainClass.splitChar));
                ArrayList<Double> confidences = new ArrayList<>();
                confidencesStr.forEach(s -> confidences.add(Double.parseDouble(s)));
                Double maxConfidence = confidences.get(0);
                double times = 1;
                while (maxConfidence * 1.25 < 1) {
                    maxConfidence = maxConfidence * 1.25;
                    times *= 1.25;
                }
                for (int i = 0; i < confidences.size(); i++) {
                    confidences.set(i, confidences.get(i) * times);
                }
                if (analysisObject.equals("keywords")) {
                    ArrayList<Association> associationTopics = new ArrayList<>();
                    for (int index = 0; index < associationItems.length; index++) {
                        Association associationTopic = new Association(associationItems[index], confidences.get(index));
                        associationTopics.add(associationTopic);
                    }
                    Topic topic = topicRepository.findTopicByName(item);
                    assert topic != null;
                    topic.setAssociationTopics(associationTopics);
                    topicRepository.save(topic);
                } else {
                    ArrayList<Association> associationSubjects = new ArrayList<>();
                    for (int index = 0; index < associationItems.length; index++) {
                        Association associationTopic = new Association(associationItems[index], confidences.get(index));
                        associationSubjects.add(associationTopic);
                    }
                    Subject subject = subjectRepository.findSubjectByName(item);
                    assert subject != null;
                    subject.setAssociationSubjects(associationSubjects);
                    subjectRepository.save(subject);
                }
            }
            StatusCtrl.changeRunningStatusTo("ALl data has been written to ElasticSearch!", name);
        } catch (Exception e) {
            e.printStackTrace();
            fileReader.close();
        } finally {
            fileReader.close();
        }
    }

    private void deleteDir(String dirPath) throws IOException {
        File resultDir = new File(dirPath);
        if (resultDir.exists()) {
            if (resultDir.isDirectory()) {
                File[] sonDirs = resultDir.listFiles();
                assert sonDirs != null;
                for (File dir : sonDirs) {
                    File[] tmpFiles = dir.listFiles();
                    assert tmpFiles != null;
                    for (File file : tmpFiles) {
                        if (!file.delete()) {
                            StatusCtrl.changeRunningStatusToStop("Can't delete tmp files at" + file.getPath(), name);
                            throw new IOException(analysisObject + " analysis: Can't delete tmp files at" + file.getPath());
                        }
                    }
                    if (!dir.delete()) {
                        StatusCtrl.changeRunningStatusToStop("Can't delete tmp files" + dir.getPath(), name);
                        throw new IOException(analysisObject + " analysis: Can't delete tmp files at" + dir.getPath());
                    }
                }
            }
            if (!resultDir.delete()) {
                StatusCtrl.changeRunningStatusToStop("Can't delete files at" + resultDir.getPath(), name);
                throw new IOException(analysisObject + " analysis: Can't delete files at" + resultDir.getPath());
            }
            log.info("Deleted temp files at " + dirPath);
        }
    }

    private boolean threadStopCheck(String name) throws IOException {
        if (StatusCtrl.isStopped(name)) {
            interruptStop("Stopped");
            return true;
        }
        return false;
    }

    private void interruptStop(String reason) throws IOException {
        if (deleteTmpFiles) {
            deleteDir(inputPath);
            deleteDir(resultDir);
        }
        StatusCtrl.changeRunningStatusToStop(reason, name);
    }
}
