package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.model.web.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${schedule.cache-directory}")
    private String cacheDirectory;

    @Override
    public boolean start() {
        if (StatusCtrl.hasRunningJob())
            return false;
        StatusCtrl statusCtrl = new StatusCtrl()
                .setTemplate(template)
                .setTopicRepository(topicRepository)
                .setSubjectRepository(subjectRepository)
                .setCacheDirectory(cacheDirectory);
        new Thread(statusCtrl, "schedule-ctrl").start();
        return true;
    }

    @Override
    public Schedule getStatus() {
        return StatusCtrl.getStatus();
    }

    @Override
    public void stop() {
        StatusCtrl.stop();
    }

    @Override
    public void autoStart(String cron) {
        StatusCtrl.cron = cron;
    }

}
