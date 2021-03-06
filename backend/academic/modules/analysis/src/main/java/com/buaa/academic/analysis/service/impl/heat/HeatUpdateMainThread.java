package com.buaa.academic.analysis.service.impl.heat;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class HeatUpdateMainThread implements Runnable {
    public static Map<String, Integer> total = new ConcurrentHashMap<>();
    public static Map<String, Integer> finished = new ConcurrentHashMap<>();
    public static Map<String, ConcurrentLinkedQueue<Terms.Bucket>> targetTerm =  new HashMap<>();
    public static Map<Integer, Double> rate = new ConcurrentHashMap<>();
    public static Map<String, Boolean> AggEnd = new HashMap<>();

    private String name;
    private String targetIndex;
    private int jobsNum;
    private final ElasticsearchRestTemplate template;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;

    public HeatUpdateMainThread(ElasticsearchRestTemplate template) {
        this.template = template;
    }
    
    public HeatUpdateMainThread setName(String name) {
        this.name = name;
        return this;
    }

    public HeatUpdateMainThread setJobsNum(int jobsNum) {
        this.jobsNum = jobsNum;
        return this;
    }

    public HeatUpdateMainThread setTargetIndex(String targetIndex) {
        this.targetIndex = targetIndex;
        return this;
    }

    public HeatUpdateMainThread setTopicRepository(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        return this;
    }

    public HeatUpdateMainThread setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
        return this;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("Heat update started");
        AggEnd.put(name, false);
        ConcurrentLinkedQueue<Terms.Bucket> buckets = new ConcurrentLinkedQueue<>();
        targetTerm.put(name, buckets);
        total.put(name, 0);
        finished.put(name, 0);

        StatusCtrl.changeRunningStatusTo("Building threads...", name);
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < jobsNum; i++) {
            Thread thread = new Thread(new HeatCalThread(name)
                    .setSubjectRepository(subjectRepository)
                    .setTopicRepository(topicRepository));
            threads.add(thread);
            thread.start();
        }

        long start_time = System.currentTimeMillis();

        StatusCtrl.changeRunningStatusTo("Rate rules generating...", name);
        rateGen();

        StatusCtrl.changeRunningStatusTo("Aggregating " + targetIndex + "...", name);

        long totalCount;
        NativeSearchQuery countTotalWordQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .build();
        if (targetIndex.equals("keywords")) {
            totalCount = template.count(countTotalWordQuery, Topic.class);
        } else {
            totalCount = template.count(countTotalWordQuery, Subject.class);
        }
        total.put(name, (int) totalCount);
        finished.put(name, 0);

        int bucketSize = 1000;
        int partitionNum = (int) ((totalCount + bucketSize - 1) / bucketSize);

        int totalNum = 0;
        for (int partition = 0; partition < partitionNum; partition++) {

            ValueCountAggregationBuilder count = new ValueCountAggregationBuilder("count").field("year");
            TermsAggregationBuilder yearAgg = new TermsAggregationBuilder("year_term")
                    .field("year").subAggregation(count).order(BucketOrder.key(true)).size(200);

            TermsAggregationBuilder term = new TermsAggregationBuilder("term").field(targetIndex + ".raw")
                    .subAggregation(yearAgg).size(1500).includeExclude(new IncludeExclude(partition, partitionNum));

            NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .addAggregation(term)
                    .withMaxResults(0)
                    .build();
            SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
            Aggregations aggregations = searchHit.getAggregations();
            assert aggregations != null;
            Aggregation aggregation = aggregations.asMap().get("term");
            ParsedStringTerms terms = (ParsedStringTerms) aggregation;

            totalNum += terms.getBuckets().size();
            log.info("batch size: {}", totalNum);

            buckets.addAll(terms.getBuckets());
        }

        synchronized (StatusCtrl.STATUS_LOCK) {
            AggEnd.put(name, true);
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            StatusCtrl.changeRunningStatusToStop(e.toString(), name);
        }

        if (StatusCtrl.isStopped(name)) {
            StatusCtrl.changeRunningStatusToStop("Stopped.", name);
            return;
        }

        double costTime = ((double) (System.currentTimeMillis() - start_time) / 1000);
        StatusCtrl.changeRunningStatusToStop("All done! Cost " + costTime + "s. ", name);

        log.info("Heat update finished");
    }

    private void rateGen() {
        Calendar calendar = Calendar.getInstance();
        int minYear = 1900;
        int maxYear = calendar.get(Calendar.YEAR);
        int farYear = maxYear - 4;
        int recentYear = maxYear - 2;
        int a = maxYear - minYear + 3 * farYear - 3 * recentYear;
        int b = 3 * minYear - 6 * farYear + 3 * recentYear;
        int c = -3 * minYear + 3 * farYear;
        for (int year = minYear; year <= maxYear; year++) {
            double t = solve(a, b, c, minYear - year);
            double rate = 3 * (1 - t) * Math.pow(t, 3) + Math.pow(t, 3);
            HeatUpdateMainThread.rate.put(year, rate);
        }
    }

    private static double solve(int a, int b, int c, int d) {
        double A, B, C;
        A = Math.pow(b, 2) - 3 * a * c;
        B = b * c - 9 * a * d;
        C = Math.pow(c, 2) - 3 * b * d;

        double judge = Math.pow(B, 2) - 4 * A * C;

        if (A == B && A == 0) {
            return ((double) -b) / 3 * (a);
        } else if (judge > 0) {
            double tmp = Math.sqrt(judge);
            double Y1 = A * b + 3 * a * (-B + tmp) / 2;
            double Y2 = A * b + 3 * a * (-B - tmp) / 2;
            double y1 = Y1 < 0 ? -Math.pow(-Y1, 1 / 3.0) : Math.pow(Y1, 1 / 3.0);
            double y2 = Y2 < 0 ? -Math.pow(-Y2, 1 / 3.0) : Math.pow(Y2, 1 / 3.0);
            return (-b - y1 - y2) / (3 * a);
        } else if (judge == 0) {
            double K = B / A;
            double x1 = ((double) -b) / a + K;
            if (0 <= x1 && x1 <= 1)
                return x1;
            return -K / 2;
        }
        double T = (2 * A * b - 3 * a * B) / (2 * Math.sqrt(Math.pow(A, 3)));
        double theta = Math.acos(T);
        double x1 = (-b - 2 * Math.sqrt(A) * Math.cos(theta / 3)) / (3 * a);
        if (0 <= x1 && x1 <= 1)
            return x1;
        double v = Math.sqrt(3) * Math.sin(theta / 3);
        double x2 = (-b + Math.sqrt(A) * (Math.cos(theta / 3) + v)) / (3 * a);
        if (0 <= x2 && x2 <= 1)
            return x2;
        return (-b + Math.sqrt(A) * (Math.cos(theta / 3) - v)) / (3 * a);
    }


}
