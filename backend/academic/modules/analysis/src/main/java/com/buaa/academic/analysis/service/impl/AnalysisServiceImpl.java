package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.Status;
import com.buaa.academic.analysis.service.AnalysisService;
import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import com.buaa.academic.analysis.service.impl.fpg.StatusCtrl;
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

    @Override
    public boolean associationAnalysis() {

        FPGMainClass topicFPG = new FPGMainClass("topics")
                .setName(JobClass.TOPIC_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false)
                .setTemplate(template);
        Thread topicFPGThread = new Thread(topicFPG);
        topicFPGThread.setName(JobClass.TOPIC_FPG_ANALYSIS.name());
        synchronized (StatusCtrl.STATUS_LOCK) {
            if (StatusCtrl.isRunning.containsKey(JobClass.TOPIC_FPG_ANALYSIS.name()))
                return false;
            StatusCtrl.isRunning.put(JobClass.TOPIC_FPG_ANALYSIS.name(), true);
        }
        topicFPGThread.start();

        FPGMainClass subjectFPG = new FPGMainClass("subjects")
                .setName(JobClass.SUBJECT_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false)
                .setTemplate(template);
        Thread subjectFPGThread = new Thread(subjectFPG);
        subjectFPGThread.setName(JobClass.SUBJECT_FPG_ANALYSIS.name());
        synchronized (StatusCtrl.STATUS_LOCK) {
            if (StatusCtrl.isRunning.containsKey(JobClass.SUBJECT_FPG_ANALYSIS.name()))
                return false;
            StatusCtrl.isRunning.put(JobClass.SUBJECT_FPG_ANALYSIS.name(), true);
        }
        subjectFPGThread.start();

        return true;
    }

    @Override
    public Status getStatus() {
        Status status = new Status();
        Map<String, String> jobs = new HashMap<>();
        synchronized (StatusCtrl.STATUS_LOCK) {
            for (String job : StatusCtrl.runningStatus.keySet()) {
                jobs.put(job, StatusCtrl.runningStatus.get(job));
            }
        }
        for (Map.Entry<String, String> entry : jobs.entrySet()) {
            status.addJObStatus(new Status.JobStatus(entry.getKey(), entry.getValue()));
        }
        return status;
    }

    @Override
    public void associationStop() {
        synchronized (StatusCtrl.STATUS_LOCK) {
            StatusCtrl.isRunning.replaceAll((j, v) -> false);
        }
    }

}
