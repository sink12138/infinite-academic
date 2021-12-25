package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.model.web.Task;
import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.model.queueObject.PatentObject;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import com.buaa.academic.spider.repository.*;
import com.buaa.academic.spider.service.Impl.*;
import com.buaa.academic.tool.util.NaturalCron;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

@Component
@NoArgsConstructor
@Data
@Slf4j
public class StatusCtrl {
    public static final Object queueLock = new Object();

    public static Queue<String> paperKeywordQueue = new ConcurrentLinkedQueue<>();
    public static Queue<String> patentKeywordQueue = new ConcurrentLinkedQueue<>();
    public static Queue<PaperObject> paperObjectQueue = new PriorityBlockingQueue<>();
    public static Queue<ResearcherSet> researcherQueue = new ConcurrentLinkedQueue<>();
    public static Queue<Researcher> interestsQueue = new ConcurrentLinkedQueue<>();
    public static Queue<PaperObject> subjectsQueue = new PriorityBlockingQueue<>();
    public static Queue<String> journalUrls = new ConcurrentLinkedQueue<>();
    public static Queue<PaperObject> sourceQueue = new PriorityBlockingQueue<>();
    public static Queue<PatentObject> patentObjectQueue = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String, String> runningStatus = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> runningJob = new ConcurrentHashMap<>();
    public static int runningPapersInitThreadNum = 0;
    public static int runningPatentsInitThreadNum = 0;
    public static int runningMainInfoThreadNum = 0;
    public static boolean jobStopped = false;
    public static boolean paperScrollEnd = false;
    public static Date lastRun;
    public static Date nextRunningDate;
    public static String cron = "0 0 18 * * ?";
    public static int paperDurationHours = 3;
    public static int patentDurationHours = 2;

    @Slf4j
    public static class ErrorHandler implements Runnable {

        private int errorNum;

        public synchronized void report(Exception e) {
            ++errorNum;
            log.info("Reported error ({} recent): {}", errorNum, e.getMessage());
        }

        @Override
        public void run() {
            /* Shut down all threads if number of errors reaches 30 in 5 minutes */
            while (runningJob.isEmpty()) {
                Thread.onSpinWait();
            }
            int threshold = 30;
            int period = 300;
            for (int loop = 0; errorNum < threshold; loop = (loop + 1) % period) {
                if (jobStopped || runningJob.isEmpty()) {
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
    public static class QueueCounter implements Runnable {

        private long interval = 60000;

        private String summaryForPaper() {
            return String.format("Paper Queued items: %d keyword(s), %d paper(s), %d researcher(s), %d interest(s), %d subject(s), %d journal(s), %d source(s)",
                    paperKeywordQueue.size(), paperObjectQueue.size(), researcherQueue.size(), interestsQueue.size(),
                    subjectsQueue.size(), journalUrls.size(), sourceQueue.size());
        }
        private String summaryForPatent() {
            return String.format("Patent Queued items: %d keyword(s), %d patent(s)",
                    patentKeywordQueue.size(), patentObjectQueue.size());
        }

        @Override
        public void run() {
            while (runningJob.isEmpty()) {
                Thread.onSpinWait();
            }
            while (!jobStopped && !runningJob.isEmpty()) {
                log.info(summaryForPaper());
                log.info(summaryForPatent());
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
    public EsUtil esUtil;

    @Autowired
    public PaperRepository paperRepository;

    @Autowired
    public InstitutionRepository institutionRepository;

    @Autowired
    public JournalRepository journalRepository;

    @Autowired
    public ResearcherRepository researcherRepository;

    @Autowired
    public PatentRepository patentRepository;

    private int papersInitThreadNum;

    private int paperMainInfoNum;

    private int subjectTopicThreadNum;

    private int researcherThreadNum;

    private int interestsThreadNum;

    private int journalThreadNum;

    private int paperSourceThreadNum;

    private int patentsInitThreadNum;

    private int patentMainInfoNum;

    public boolean start() {
        if (runningJob.size() > 0)
            return false;
        startInit();
        List<String> defaultKeywords = List.of("操作系统", "平板电脑", "快充技术", "通信设备", "定位设备", "人脸识别", "指纹识别", "虹膜识别", "声纹识别");
        StatusCtrl.paperKeywordQueue.addAll(defaultKeywords);
        StatusCtrl.patentKeywordQueue.addAll(defaultKeywords);
        boolean headless = true;

        new Thread(errorHandler, "Error-Handler").start();
        new Thread(queueCounter, "Queue-Counter").start();
        for (int i = 0; i < papersInitThreadNum; i++) {
            Thread thread = new Thread(new PaperQueueInitThread(this, headless));
            String threadName = "Paper-Init-" + i;
            runningJob.put(threadName, true);
            thread.setName(threadName);
            thread.start();
        }
        for (int i = 0; i < patentsInitThreadNum; i++) {
            Thread thread = new Thread(new PatentQueueInitThread(this, headless));
            String threadName = "Patent-Init-" + i;
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
        for (Boolean running : runningJob.values()) {
            if (running) {
                isRunning = true;
                break;
            }
        }
        schedule.setRunning(isRunning);
        schedule.setLastRun(lastRun);
        schedule.setNextRun(nextRunningDate);
        schedule.setFrequency(NaturalCron.describe(cron));
        for (String threadName : runningStatus.keySet()) {
            schedule.addTask(new Task(threadName, runningStatus.get(threadName)));
        }
        return schedule;
    }

    @AllArgsConstructor
    private class FixThread implements Runnable {
        @Override
        public void run() {
            log.info("Scroll paper...");
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("crawled", true))
                            .mustNot(QueryBuilders.existsQuery("authors.id")))
                    .withPageable(PageRequest.of(0, 500))
                    .withFields("title")
                    .build();
            SearchScrollHits<Paper> searchHit = template.searchScrollStart(3000, query, Paper.class, IndexCoordinates.of("paper"));
            String scrollId = searchHit.getScrollId();
            while (searchHit.hasSearchHits()) {
                for (SearchHit<Paper> paperHit : searchHit.getSearchHits()) {
                        StatusCtrl.paperKeywordQueue.add(paperHit.getContent().getTitle());
                }
                searchHit = template.searchScrollContinue(scrollId, 3000, Paper.class, IndexCoordinates.of("paper"));
            }
            log.info("{} papers without authors id need to be crawled", paperKeywordQueue.size());

            StatusCtrl.startInit();

            boolean headless = true;

            new Thread(errorHandler, "Error-Handler").start();
            new Thread(queueCounter, "Queue-Counter").start();
            for (int i = 0; i < papersInitThreadNum; i++) {
                Thread thread = new Thread(new SpiderOneQueueThread(StatusCtrl.this, headless));
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

        new Thread(new FixThread(), "AuthorsIdFix").start();

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
        for (int i = 0; i < paperMainInfoNum; i++) {
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

        for (int i = 0; i < patentMainInfoNum; i++) {
            Thread thread = new Thread(new PatentMainInfoThread(this, headless));
            String threadName = "Patent-Main-" + i;
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
        StatusCtrl.subjectsQueue.clear();
        StatusCtrl.runningJob.clear();
        StatusCtrl.runningStatus.clear();
    }

    public Boolean isRunning() {
        return runningJob.size() > 0;
    }

}
