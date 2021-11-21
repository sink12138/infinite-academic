package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.Status;
import com.buaa.academic.analysis.service.AnalysisService;
import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

enum JobClass {
    TOPIC_FPG_ANALYSIS,
    SUBJECT_FPG_ANALYSIS
}

@Service
public class AnalysisServiceImpl implements AnalysisService {
    @Autowired
    private ElasticsearchRestTemplate template;

    public static final Object STATUS_LOCK = new Object();

    @Override
    public boolean associationAnalysis() {

        FPGMainClass topicFPG = new FPGMainClass()
                .setName(JobClass.TOPIC_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false).setAnalysisObject("topics")
                .setTemplate(template);
        Thread topicFPGThread = new Thread(topicFPG);
        synchronized (STATUS_LOCK) {
            if (StatusResources.isRunning.containsKey(JobClass.TOPIC_FPG_ANALYSIS.name())
                    && StatusResources.isRunning.get(JobClass.TOPIC_FPG_ANALYSIS.name()))
                return false;
            StatusResources.isRunning.put(JobClass.TOPIC_FPG_ANALYSIS.name(), true);
            StatusResources.currentJob.put(JobClass.TOPIC_FPG_ANALYSIS.name(), topicFPGThread);
        }
        topicFPGThread.start();

        FPGMainClass subjectFPG = new FPGMainClass()
                .setName(JobClass.SUBJECT_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false).setAnalysisObject("subjects")
                .setTemplate(template);
        Thread subjectFPGThread = new Thread(subjectFPG);
        synchronized (STATUS_LOCK) {
            if (StatusResources.isRunning.containsKey(JobClass.SUBJECT_FPG_ANALYSIS.name())
                    && StatusResources.isRunning.get(JobClass.SUBJECT_FPG_ANALYSIS.name()))
                return false;
            StatusResources.isRunning.put(JobClass.SUBJECT_FPG_ANALYSIS.name(), true);
            StatusResources.currentJob.put(JobClass.SUBJECT_FPG_ANALYSIS.name(), subjectFPGThread);
        }
        subjectFPGThread.start();

        return true;
    }

    @Override
    public Status getStatus() {
        Status status = new Status();
        Map<String, String> jobs = new HashMap<>();
        synchronized (STATUS_LOCK) {
            for (String job : StatusResources.currentJob.keySet()) {
                jobs.put(job, StatusResources.runningStatus.get(job));
            }
        }
        for (Map.Entry<String, String> entry : jobs.entrySet()) {
            status.addJObStatus(new Status.JobStatus(entry.getKey(), entry.getValue()));
        }
        return status;
    }

    @Override
    public boolean associationStop() {
        try {
            for (Thread job : StatusResources.currentJob.values()) {
                job.stop();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
