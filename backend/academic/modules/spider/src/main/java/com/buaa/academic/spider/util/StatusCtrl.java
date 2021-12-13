package com.buaa.academic.spider.util;

import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.model.web.Task;
import com.buaa.academic.spider.model.queueObject.JournalObject;
import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import com.buaa.academic.spider.repository.InstitutionRepository;
import com.buaa.academic.spider.repository.JournalRepository;
import com.buaa.academic.spider.repository.PaperRepository;
import com.buaa.academic.spider.repository.ResearcherRepository;
import com.buaa.academic.spider.service.ExistenceService;
import com.buaa.academic.spider.service.Impl.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@NoArgsConstructor
@Data
public class StatusCtrl {
    public static final Object queueLock = new Object();

    public static ConcurrentLinkedQueue<PaperObject> paperObjectQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ResearcherSet> researcherQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> subjectAndTopicCrawlerQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<JournalObject> journalUrls = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> sourceQueue = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String, String> runningStatus = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> runningJob = new ConcurrentHashMap<>();
    public static int runningQueueInitThreadNum = 0;
    public static int runningMainInfoThreadNum = 0;
    public static boolean jobStopped = false;
    public static Date lastRun;

    @Autowired
    public ElasticsearchRestTemplate template;

    @Autowired
    public ExistenceService existenceService;

    @Autowired
    public PaperRepository paperRepository;

    @Autowired
    public InstitutionRepository institutionRepository;

    @Autowired
    public JournalRepository journalRepository;

    @Autowired
    public ResearcherRepository researcherRepository;

    private List<String> keywords = List.of("信息");

    private int mainInfoThreadNum;

    private int subjectTopicThreadNum;

    private int researcherThreadNum;

    private int journalThreadNum;

    private int paperSourceThreadNum;

    public Boolean start() {
        if (runningJob.size() > 0)
            return false;
        lastRun = new Date();
        StatusCtrl.jobStopped = false;
        StatusCtrl.paperObjectQueue.clear();
        StatusCtrl.subjectAndTopicCrawlerQueue.clear();
        StatusCtrl.runningJob.clear();
        StatusCtrl.runningStatus.clear();
        Boolean headless = true;
        for (String keyword: keywords) {
            Thread thread = new Thread(new CrawlerQueueInitThread(keyword, this, headless));
            String threadName = "QueueInitThread-keyword:" + keyword;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < paperSourceThreadNum; i++) {
            Thread thread = new Thread(new PaperSourceThread(this, headless));
            String threadName = "Source-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < mainInfoThreadNum; i++) {
            Thread thread = new Thread(new PaperMainInfoThread(this, headless));
            String threadName = "PaperMainInfoThread-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < subjectTopicThreadNum; i++) {
            Thread thread = new Thread(new SubjectTopicThread(this, headless));
            String threadName = "SubjectTopicThread-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < researcherThreadNum; i++) {
            Thread thread = new Thread(new ResearcherCrawlerThread(this, headless));
            String threadName = "ResearcherThread-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < journalThreadNum; i++) {
            Thread thread = new Thread(new JournalCrawlThread(this, headless));
            String threadName = "JournalThread-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        return true;
    }

    public void stop() {
        StatusCtrl.jobStopped = true;
    }

    public Schedule getStatus() {
        Schedule schedule = new Schedule();
        schedule.setName("爬虫");
        boolean isRunning = false;
        for (Boolean running: runningJob.values()) {
            if (running) {
                isRunning = true;
                break;
            }
        }
        schedule.setRunning(isRunning);
        schedule.setLastRun(lastRun);
        for (String threadName: runningStatus.keySet()) {
            schedule.addTask(new Task(threadName, runningStatus.get(threadName)));
        }
        return schedule;
    }

    public void changeRunningStatusTo(String threadName, String status) {
        runningStatus.put(threadName, status);
    }

    public void changeRunningStatusStop(String threadName, String status) {
        runningJob.remove(threadName);
        runningStatus.put(threadName, status);
    }

    public Boolean isRunning() {
        return runningJob.size() > 0;
    }

}
