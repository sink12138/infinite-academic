package com.buaa.academic.analysis.service.impl.hot;

import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.entity.Paper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HotUpdateMainThread implements Runnable{
    public static Map<String, Integer> total = new HashMap<>();
    public static Map<String, Integer> finished = new HashMap<>();
    public static ParsedStringTerms targetTerm = null;

    private String targetIndex;
    private Integer jobsNum;
    private String threadName;
    private final ElasticsearchRestTemplate template;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;

    public HotUpdateMainThread(ElasticsearchRestTemplate template) {
        this.template = template;
    }

    public HotUpdateMainThread setJobsNum(Integer jobsNum) {
        this.jobsNum = jobsNum;
        return this;
    }

    public HotUpdateMainThread setThreadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public HotUpdateMainThread setTargetIndex(String targetIndex) {
        this.targetIndex = targetIndex;
        return this;
    }

    public HotUpdateMainThread setTopicRepository(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        return this;
    }

    public HotUpdateMainThread setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
        return this;
    }

    @Override
    public void run() {
        StatusCtrl.changeRunningStatusTo(threadName, "Building search query...");

        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder("count").field("year");
        TermsAggregationBuilder termsAgg = new TermsAggregationBuilder("year_term").field("year").subAggregation(count);
        TermsAggregationBuilder topicTerm = new TermsAggregationBuilder("term").field(targetIndex + ".keyword").subAggregation(termsAgg);
        NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(topicTerm)
                .build();

        StatusCtrl.changeRunningStatusTo(threadName, "Aggregating " + targetIndex + "...");
        SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
        Aggregations aggregations = searchHit.getAggregations();
        assert aggregations != null;
        Aggregation aggregation = aggregations.asMap().get("term");
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;

        targetTerm = terms;
        total.put(threadName, terms.getBuckets().size());
        finished.put(threadName, 0);

        StatusCtrl.changeRunningStatusTo(threadName, "Building threads...");
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < jobsNum; i++) {
            threads.add(new Thread(new HotCalThread(threadName)
                    .setSubjectRepository(subjectRepository)
                    .setTopicRepository(topicRepository)));
        }

        for (Thread thread: threads) {
            thread.start();
        }

    }


}
