package com.buaa.academic.spider.util;

import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.repository.InstitutionRepository;
import com.buaa.academic.spider.repository.JournalRepository;
import com.buaa.academic.spider.repository.PaperRepository;
import com.buaa.academic.spider.repository.ResearcherRepository;
import com.buaa.academic.spider.service.ExistenceService;
import com.buaa.academic.spider.service.Impl.CrawlerQueueInitThread;
import com.buaa.academic.spider.service.Impl.PaperMainInfoThread;
import com.buaa.academic.spider.service.Impl.SubjectTopicThread;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@NoArgsConstructor
@Data
public class StatusCtrl {
    public static final Object queueLock = new Object();
    public static ConcurrentLinkedQueue<PaperObject> paperObjectQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PaperObject> subjectAndTopicCrawlerQueue = new ConcurrentLinkedQueue<>();
    public static int runningQueueInitThreadNum = 0;
    public static int runningMainInfoThreadNum = 0;
    public static boolean jobStopped = false;

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

    private ArrayList<String> keywords;

    private int mainInfoThreadNum;

    private int subjectTopicThreadNum;

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public void start() {
        StatusCtrl.jobStopped = false;
        StatusCtrl.paperObjectQueue.clear();
        for (String keyword: keywords) {
            Thread thread = new Thread(new CrawlerQueueInitThread(keyword, this));
            thread.start();
        }
        for (int i = 0; i < mainInfoThreadNum; i++) {
            new Thread(new PaperMainInfoThread(this)).start();
        }
        for (int i = 0; i < subjectTopicThreadNum; i++) {
            new Thread(new SubjectTopicThread(this)).start();
        }
    }

    public void stop() {
        StatusCtrl.jobStopped = true;
    }
}