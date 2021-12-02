package com.buaa.academic.analysis.service.impl;

import java.util.Date;
import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

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
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                if (StatusCtrl.hasRunningJob())
                    return;
                StatusCtrl statusCtrl = new StatusCtrl()
                        .setTemplate(template)
                        .setTopicRepository(topicRepository)
                        .setSubjectRepository(subjectRepository);
                new Thread(statusCtrl).start();
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                // 任务触发，可修改任务的执行周期
                CronTrigger trigger = new CronTrigger(StatusCtrl.cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                StatusCtrl.nextRunningDate = nextExec;
                return nextExec;
            }
        });
    }
}