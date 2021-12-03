package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;

@Lazy(false)
@Component
@EnableScheduling
public class ScheduleTask implements SchedulingConfigurer {
    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> {
            if (StatusCtrl.hasRunningJob())
                return;
            StatusCtrl statusCtrl = new StatusCtrl()
                    .setTemplate(template)
                    .setTopicRepository(topicRepository)
                    .setSubjectRepository(subjectRepository);
            new Thread(statusCtrl).start();
        }, triggerContext -> {
            // 任务触发，可修改任务的执行周期
            CronTrigger trigger = new CronTrigger(StatusCtrl.cron);
            Date nextExec = trigger.nextExecutionTime(triggerContext);
            StatusCtrl.nextRunningDate = nextExec;
            return nextExec;
        });
    }
}
