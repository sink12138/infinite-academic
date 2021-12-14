package com.buaa.academic.spider.util;

import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.model.web.Task;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StatusCtrl {
    public static final Object queueLock = new Object();

    public static ConcurrentLinkedQueue<String> keywordQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> paperObjectQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<ResearcherSet> researcherQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> subjectAndTopicCrawlerQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> journalUrls = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> sourceQueue = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String, String> runningStatus = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> runningJob = new ConcurrentHashMap<>();
    public static int runningQueueInitThreadNum = 0;
    public static int runningMainInfoThreadNum = 0;
    public static boolean jobStopped = false;
    public static Date lastRun;

    @Slf4j
    public static class ErrorHandler extends Thread {

        private int errorNum;

        public synchronized void report(Exception e) {
            ++errorNum;
            log.info("Reported error ({} recent): {}", errorNum, e.getMessage());
        }

        @Override
        public void run() {
            /* Shut down all threads if number of errors reaches 30 in 5 minutes */
            int threshold = 30;
            int period = 300;
            for (int loop = 0; errorNum < threshold; loop = (loop + 1) % period) {
                if (StatusCtrl.jobStopped) {
                    return;
                }
                if (loop == 0)
                    errorNum = 0;
                else if (loop % (period / 5) == 0) {
                    if (errorNum >= threshold / 3 * 2) {
                        log.warn("Number of errors reached 2/3 of the threshold within {} seconds", period);
                    }
                    else if (errorNum >= threshold / 3) {
                        log.warn("Number of errors reached 1/3 of the threshold within {} seconds", period);
                    }
                }
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.error("Shutting down tasks due to high error frequency");
            StatusCtrl.jobStopped = true;
            errorNum = 0;
        }
    }

    public static final ErrorHandler errorHandler = new ErrorHandler();

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

    private int queueInitThreadNum;

    private int mainInfoThreadNum;

    private int subjectTopicThreadNum;

    private int researcherThreadNum;

    private int journalThreadNum;

    private int paperSourceThreadNum;

    public boolean start() {
        if (runningJob.size() > 0)
            return false;
        log.info("Initializing tasks...");
        lastRun = new Date();
        StatusCtrl.jobStopped = false;
        StatusCtrl.paperObjectQueue.clear();
        StatusCtrl.subjectAndTopicCrawlerQueue.clear();
        StatusCtrl.runningJob.clear();
        StatusCtrl.runningStatus.clear();
        StatusCtrl.keywordQueue.addAll(List.of("visual", "算法", "image", "卷积", "database", "最短"));
        boolean headless = true;

        new Thread(errorHandler, "Error-Handler").start();
        for (int i = 0; i < queueInitThreadNum; i++) {
            Thread thread = new Thread(new CrawlerQueueInitThread(this, headless));
            String threadName = "QueueInit-" + i;
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
            String threadName = "Paper-Main-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < subjectTopicThreadNum; i++) {
            Thread thread = new Thread(new SubjectTopicThread(this, headless));
            String threadName = "Subject-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < researcherThreadNum; i++) {
            Thread thread = new Thread(new ResearcherCrawlerThread(this, headless));
            String threadName = "Researcher-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < journalThreadNum; i++) {
            Thread thread = new Thread(new JournalCrawlThread(this, headless));
            String threadName = "Journal-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        return true;
    }

    public void stop() {
        log.info("Stopping tasks...");
        StatusCtrl.jobStopped = true;
    }

    public Schedule getStatus() {
        Schedule schedule = new Schedule();
        schedule.setName("数据库更新与扩充");
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
