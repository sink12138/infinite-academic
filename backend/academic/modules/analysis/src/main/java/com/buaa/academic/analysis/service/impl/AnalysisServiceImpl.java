package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.Status;
import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    private final StatusCtrl statusCtrl = new StatusCtrl()
            .setTemplate(template)
            .setTopicRepository(topicRepository)
            .setSubjectRepository(subjectRepository);

    @Override
    public boolean start() {

        if (statusCtrl.hasRunningJob())
            return false;
        new Thread(statusCtrl).start();
        return true;

        /*
        FPGMainClass topicFPG = new FPGMainClass("topics")
                .setName(JobClass.TOPIC_FPG_ANALYSIS.name())
                .setMinSupport(0.4).setMinConfidence(0.6)
                .setDeleteTmpFiles(false)
                .setTemplate(template)
                .setTopicRepository(topicRepository);
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
                .setTemplate(template)
                .setSubjectRepository(subjectRepository);
        Thread subjectFPGThread = new Thread(subjectFPG);
        subjectFPGThread.setName(JobClass.SUBJECT_FPG_ANALYSIS.name());
        synchronized (StatusCtrl.STATUS_LOCK) {
            if (StatusCtrl.isRunning.containsKey(JobClass.SUBJECT_FPG_ANALYSIS.name()))
                return false;
            StatusCtrl.isRunning.put(JobClass.SUBJECT_FPG_ANALYSIS.name(), true);
        }
        subjectFPGThread.start();

        return true;*/
    }

    @Override
    public Status getStatus() {
        return statusCtrl.getStatus();
    }

    @Override
    public void stop() {
        statusCtrl.associationStop();
    }

}
