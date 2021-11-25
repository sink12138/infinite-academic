package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.Status;
import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import org.apache.hadoop.mapreduce.Job;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


enum JobClass {
    TOPIC_FPG_ANALYSIS,
    SUBJECT_FPG_ANALYSIS,
    HOT_ANALYSIS
}

public class
StatusCtrl implements Runnable{
    public static final Object STATUS_LOCK = new Object();

    public static final Map<String, Boolean> isRunning = new HashMap<>();
    public static final Map<String, String> runningStatus = new HashMap<>();
    public static final Map<String, Job> currentJob = new HashMap<>();

    private ElasticsearchRestTemplate template;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;

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

    public boolean hasRunningJob() {
        synchronized (StatusCtrl.STATUS_LOCK) {
            if (StatusCtrl.isRunning.size() > 0)
                return false;
        }
        return true;
    }

    @Override
    public void run() {
        associationAnalysis();
    }

    public Status getStatus() {
        Status status = new Status();
        Map<String, String> jobs = new HashMap<>();
        synchronized (StatusCtrl.STATUS_LOCK) {
            for (String job : StatusCtrl.runningStatus.keySet()) {
                jobs.put(job, StatusCtrl.runningStatus.get(job));
            }
        }
        for (Map.Entry<String, String> entry : jobs.entrySet()) {
            Boolean isRunning = false;
            if (StatusCtrl.isRunning.containsKey(entry.getKey())) {
                isRunning = StatusCtrl.isRunning.get(entry.getKey());
            }
            status.addJObStatus(new Status.JobStatus(entry.getKey(), entry.getValue(), isRunning));
        }
        return status;
    }

    public void associationStop() {
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.replaceAll((j, v) -> false);
        }
    }

    private void associationAnalysis() {
        FPGMainClass topicFPG = new FPGMainClass("topics")
                .setName(JobClass.TOPIC_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false)
                .setTemplate(template)
                .setTopicRepository(topicRepository);
        Thread topicFPGThread = new Thread(topicFPG);
        topicFPGThread.setName(JobClass.TOPIC_FPG_ANALYSIS.name());
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.put(JobClass.TOPIC_FPG_ANALYSIS.name(), true);
        }
        topicFPGThread.start();

        FPGMainClass subjectFPG = new FPGMainClass("subjects")
                .setName(JobClass.SUBJECT_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false)
                .setTemplate(template)
                .setSubjectRepository(subjectRepository);
        Thread subjectFPGThread = new Thread(subjectFPG);
        subjectFPGThread.setName(JobClass.SUBJECT_FPG_ANALYSIS.name());
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.put(JobClass.SUBJECT_FPG_ANALYSIS.name(), true);
        }
        subjectFPGThread.start();
    }
}