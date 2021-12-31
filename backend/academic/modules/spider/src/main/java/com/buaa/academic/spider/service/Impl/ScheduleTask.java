package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.util.StatusCtrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private StatusCtrl statusCtrl;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> {
            if (StatusCtrl.runningJob.isEmpty()) {
                statusCtrl.setPapersInitThreadNum(1);
                statusCtrl.setPaperMainInfoNum(2);
                statusCtrl.setPaperSourceThreadNum(2);
                statusCtrl.setResearcherThreadNum(2);
                statusCtrl.setInterestsThreadNum(1);
                statusCtrl.setJournalThreadNum(1);
                statusCtrl.setSubjectTopicThreadNum(1);
                statusCtrl.setPatentsInitThreadNum(0);
                statusCtrl.setPatentMainInfoNum(0);
                statusCtrl.start();
                try {
                    Thread.sleep(StatusCtrl.paperDurationHours * 3600000L);
                    statusCtrl.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                statusCtrl.setPapersInitThreadNum(0);
                statusCtrl.setPaperMainInfoNum(0);
                statusCtrl.setPaperSourceThreadNum(0);
                statusCtrl.setResearcherThreadNum(0);
                statusCtrl.setInterestsThreadNum(0);
                statusCtrl.setJournalThreadNum(0);
                statusCtrl.setSubjectTopicThreadNum(0);
                statusCtrl.setPatentsInitThreadNum(1);
                statusCtrl.setPatentMainInfoNum(2);
                statusCtrl.start();
                try {
                    Thread.sleep(StatusCtrl.patentDurationHours * 3600000L);
                    statusCtrl.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, triggerContext -> {
            // 任务触发，可修改任务的执行周期
            CronTrigger trigger = new CronTrigger(StatusCtrl.cron);
            Date nextExec = trigger.nextExecutionTime(triggerContext);
            StatusCtrl.nextRunningDate = nextExec;
            return nextExec;
        });
    }
}
