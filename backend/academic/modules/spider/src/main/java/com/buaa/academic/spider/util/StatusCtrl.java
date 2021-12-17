package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
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
    public static ConcurrentLinkedQueue<Researcher> interestsQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> subjectAndTopicCrawlerQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> journalUrls = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> sourceQueue = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String, String> runningStatus = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> runningJob = new ConcurrentHashMap<>();
    public static int runningQueueInitThreadNum = 0;
    public static int runningMainInfoThreadNum = 0;
    public static boolean jobStopped = false;
    public static boolean paperScrollEnd = false;
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
                    errorNum = 0;
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

    @Slf4j
    @Setter
    public static class QueueCounter extends Thread {

        private long interval = 600000;

        private String summary() {
            return String.format("Queued items: %d keyword(s), %d paper(s), %d researcher(s), %d interest(s), %d subject(s), %d journal(s), %d source(s)",
                    keywordQueue.size(), paperObjectQueue.size(), researcherQueue.size(), interestsQueue.size(),
                    subjectAndTopicCrawlerQueue.size(), journalUrls.size(), sourceQueue.size());
        }

        @Override
        public void run() {
            try {
                Thread.sleep(interval / 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!StatusCtrl.jobStopped && !StatusCtrl.runningJob.isEmpty()) {
                log.info(summary());
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final QueueCounter queueCounter = new QueueCounter();

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

    private int interestsThreadNum;

    private int journalThreadNum;

    private int paperSourceThreadNum;

    public boolean start() {
        if (runningJob.size() > 0)
            return false;
        startInit();
        StatusCtrl.keywordQueue.addAll(List.of("软件设计", "卷积", "计算机体系", "计算机网络", "分布式系统"));
        boolean headless = true;

        errorHandler.setName("Error-Handler");
        errorHandler.start();
        queueCounter.setName("Queue-Counter");
        queueCounter.start();
        for (int i = 0; i < queueInitThreadNum; i++) {
            Thread thread = new Thread(new CrawlerQueueInitThread(this, headless));
            String threadName = "QueueInit-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        startAfterThread(headless);
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

    @AllArgsConstructor
    private class FixThread implements Runnable {
        StatusCtrl statusCtrl;

        @Override
        public void run() {
            log.info("Scroll paper...");
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("crawled", true))
                            .must(QueryBuilders.existsQuery("authors.id")))
                    .withPageable(PageRequest.of(0, 500))
                    .withFields("title")
                    .build();
            SearchScrollHits<Paper> searchHit = template.searchScrollStart(3000, query, Paper.class, IndexCoordinates.of("paper"));
            String scrollId = searchHit.getScrollId();
            while (searchHit.hasSearchHits()) {
                for (SearchHit<Paper> paperHit: searchHit.getSearchHits()){
                    StatusCtrl.keywordQueue.add(paperHit.getContent().getTitle());
                }
                searchHit = template.searchScrollContinue(scrollId, 3000, Paper.class, IndexCoordinates.of("paper"));
            }
            log.info("{} papers without authors id", keywordQueue.size());

            StatusCtrl.startInit();

            boolean headless = true;

            errorHandler.setName("Error-Handler");
            errorHandler.start();
            queueCounter.setName("Queue-Counter");
            queueCounter.start();
            for (int i = 0; i < queueInitThreadNum; i++) {
                Thread thread = new Thread(new SpiderOneQueueThread(statusCtrl, headless));
                String threadName = "SpiderOneQueueInit-" + i;
                runningJob.put(threadName, true);
                thread.setName(threadName);
                thread.start();
            }
            startAfterThread(headless);
        }
    }

    public boolean fixResearcherId() {
        if (runningJob.size() > 0)
            return false;

        new Thread(new FixThread(this), "AuthorsIdFix").start();

        return true;
    }

    public void changeRunningStatusTo(String threadName, String status) {
        runningStatus.put(threadName, status);
    }

    public void changeRunningStatusStop(String threadName, String status) {
        runningJob.remove(threadName);
        runningStatus.put(threadName, status);
    }

    private void startAfterThread(boolean headless) {
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
        for (int i = 0; i < interestsThreadNum; ++i) {
            Thread thread = new Thread(new InterestsGIndexThread(this, headless));
            String threadName = "Interests-" + i;
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
    }

    private static void startInit() {
        log.info("Initializing tasks...");
        lastRun = new Date();
        StatusCtrl.jobStopped = false;
        StatusCtrl.paperObjectQueue.clear();
        StatusCtrl.subjectAndTopicCrawlerQueue.clear();
        StatusCtrl.runningJob.clear();
        StatusCtrl.runningStatus.clear();
    }

    public Boolean isRunning() {
        return runningJob.size() > 0;
    }

}
