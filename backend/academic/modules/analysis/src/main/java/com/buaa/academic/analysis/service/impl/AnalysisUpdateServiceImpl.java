package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.Status;
import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.AnalysisUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisUpdateServiceImpl implements AnalysisUpdateService {
    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public boolean start() {
        if (StatusCtrl.hasRunningJob())
            return false;
        StatusCtrl statusCtrl = new StatusCtrl()
                .setTemplate(template)
                .setTopicRepository(topicRepository)
                .setSubjectRepository(subjectRepository);
        new Thread(statusCtrl).start();
        return true;
    }

    @Override
    public Status getStatus() {
        return StatusCtrl.getStatus();
    }

    @Override
    public void stop() {
        StatusCtrl.associationStop();
    }

}
