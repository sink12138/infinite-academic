package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.heat.HeatUpdateMainThread;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.model.web.Task;
import com.buaa.academic.tool.util.NaturalCron;
import lombok.SneakyThrows;
import org.apache.hadoop.mapreduce.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatusCtrl implements Runnable {
    public static final Object STATUS_LOCK = new Object();

    public static final Map<String, Boolean> isRunning = new HashMap<>();
    public static final Map<String, String> runningStatus = new HashMap<>();
    public static final Map<String, Job> currentJob = new HashMap<>();
    public static boolean analysisStarted = false;
    public static String cron = "0 0 23 * * ?";
    public static Date nextRunningDate;
    private static Date lastRunningDate;

    private ElasticsearchRestTemplate template;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;
    private String cacheDirectory;

    private static final Logger logger = LoggerFactory.getLogger(StatusCtrl.class);

    public StatusCtrl setTemplate(ElasticsearchRestTemplate template) {
        this.template = template;
        return this;
    }

    public StatusCtrl setTopicRepository(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        return this;
    }

    public StatusCtrl setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
        return this;
    }

    public StatusCtrl setCacheDirectory(String directory) {
        this.cacheDirectory = directory;
        return this;
    }

    public static boolean isStopped(String threadName) {
        boolean res;
        synchronized (STATUS_LOCK) {
            res = isRunning.getOrDefault(threadName, false);
        }
        return !res;
    }

    public static void stop(String jobName) throws IOException {
        Job job = currentJob.get(jobName);
        job.close();
    }

    public static boolean hasRunningJob() {
        boolean hasRunningJob;
        synchronized (StatusCtrl.STATUS_LOCK) {
            hasRunningJob = analysisStarted;
        }
        return hasRunningJob;
    }

    public static void changeRunningStatusTo(String runningStatus, String threadName) {
        synchronized (StatusCtrl.STATUS_LOCK) {
            logger.info("Status change: " + threadName + " -> " + runningStatus);
            StatusCtrl.runningStatus.put(threadName, runningStatus);
        }
    }

    public static void changeRunningStatusToStop(String runningStatus, String threadName) {
        synchronized (StatusCtrl.STATUS_LOCK) {
            logger.info("Status change: " + threadName + " -> " + runningStatus);
            StatusCtrl.isRunning.remove(threadName);
            StatusCtrl.runningStatus.put(threadName, runningStatus);
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        logger.info("Started schedule task");
        isRunning.clear();
        currentJob.clear();
        runningStatus.clear();
        lastRunningDate = new Date();
        analysisStarted = true;
        associationAnalysis();
        heatRankAnalysis();
        analysisStarted = false;
        logger.info("Finished schedule task");
    }

    public static Schedule getStatus() {
        Schedule schedule = new Schedule();
        boolean scheduleRunning = false;
        Map<String, String> jobs = new HashMap<>();
        Map<String, Boolean> isR;
        synchronized (STATUS_LOCK) {
            for (String job : runningStatus.keySet()) {
                jobs.put(job, runningStatus.get(job));
            }
            isR = new HashMap<>(isRunning);
        }
        for (Map.Entry<String, String> entry : jobs.entrySet()) {
            Boolean isRunning = false;
            if (isR.containsKey(entry.getKey())) {
                isRunning = isR.get(entry.getKey());
            }
            if (isRunning)
                scheduleRunning = true;
            schedule.addTask(new Task(entry.getKey(), entry.getValue()));
        }
        schedule.setName("数据分析");
        schedule.setLastRun(lastRunningDate);
        schedule.setNextRun(nextRunningDate);
        schedule.setFrequency(NaturalCron.describe(cron));
        schedule.setRunning(scheduleRunning);
        return schedule;
    }

    public static void associationStop() {
        synchronized (STATUS_LOCK) {
            isRunning.replaceAll((j, v) -> false);
            analysisStarted = false;
        }
    }

    private void associationAnalysis() throws InterruptedException {
        logger.info("Association analysis started");

        double minSupport = 0.4;
        double minConfidence = 0.6;

        FPGMainClass topicFPG = new FPGMainClass("topics")
                .setName(JobType.TOPIC_FPG_ANALYSIS.name())
                .setMinSupport(minSupport).setMinConfidence(minConfidence)
                .setDeleteTmpFiles(true)
                .setTemplate(template)
                .setTopicRepository(topicRepository)
                .setCacheDirectory(cacheDirectory);
        Thread topicFPGThread = new Thread(topicFPG);
        topicFPGThread.setName("fpg-topic");
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.put(JobType.TOPIC_FPG_ANALYSIS.name(), true);
        }
        topicFPGThread.start();

        FPGMainClass subjectFPG = new FPGMainClass("subjects")
                .setName(JobType.SUBJECT_FPG_ANALYSIS.name())
                .setMinSupport(minSupport).setMinConfidence(minConfidence)
                .setDeleteTmpFiles(true)
                .setTemplate(template)
                .setSubjectRepository(subjectRepository)
                .setCacheDirectory(cacheDirectory);
        Thread subjectFPGThread = new Thread(subjectFPG);
        subjectFPGThread.setName("fpg-subject");
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.put(JobType.SUBJECT_FPG_ANALYSIS.name(), true);
        }
        subjectFPGThread.start();

        topicFPGThread.join();
        subjectFPGThread.join();

        logger.info("Association analysis finished");
    }

    private void heatRankAnalysis() throws InterruptedException {
        logger.info("Heat rank analysis started");

        int jobNumber = 10;

        HeatUpdateMainThread topicMainThread = new HeatUpdateMainThread(template)
                .setTopicRepository(topicRepository)
                .setJobsNum(jobNumber)
                .setTargetIndex("topics");
        Thread topicThread = new Thread(topicMainThread);
        topicThread.setName("heat-topic");
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.put(JobType.HOT_TOPIC_ANALYSIS.name(), true);
        }
        topicThread.start();

        HeatUpdateMainThread subjectMainThread = new HeatUpdateMainThread(template)
                .setSubjectRepository(subjectRepository)
                .setJobsNum(jobNumber)
                .setTargetIndex("subjects");
        Thread subjectThread = new Thread(subjectMainThread);
        subjectThread.setName("heat-subject");
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.put(JobType.HOT_SUBJECT_ANALYSIS.name(), true);
        }
        subjectThread.start();

        topicThread.join();
        subjectThread.join();

        logger.info("Heat rank analysis finished");
    }
}