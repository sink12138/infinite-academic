package com.buaa.academic.analysis.service.impl.hot;

import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.entity.Paper;
import lombok.SneakyThrows;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HotUpdateMainThread implements Runnable{
    public static Map<String, Integer> total = new HashMap<>();
    public static Map<String, Integer> finished = new HashMap<>();
    public static ParsedStringTerms targetTerm = null;
    public static Map<Integer, Double> rate = new HashMap<>();

    private String targetIndex;
    private Integer jobsNum;
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

    @SneakyThrows
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        long start_time = System.currentTimeMillis();

        StatusCtrl.changeRunningStatusTo(threadName, "Rate rules generating...");
        rateGen();

        StatusCtrl.changeRunningStatusTo(threadName, "Building search query...");

        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder("count").field("year");
        TermsAggregationBuilder termsAgg = new TermsAggregationBuilder("year_term").field("year").subAggregation(count);
        TermsAggregationBuilder topicTerm = new TermsAggregationBuilder("term").field(targetIndex + ".raw").subAggregation(termsAgg);
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

        try {
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            StatusCtrl.changeRunningStatusToStop(e.toString(), threadName);
        }

        if (finished.get(threadName) < total.get(threadName)) {
            StatusCtrl.changeRunningStatusToStop("Stopped.", threadName);
            return;
        }

        double costTime = ((double)(System.currentTimeMillis() - start_time) / 1000);
        StatusCtrl.changeRunningStatusToStop("All down! Cost " + costTime + "s", threadName);

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
        for (int year = minYear; year <= maxYear; year++){
            double t = solve(a, b, c, minYear - year);
            double rate = 3 * (1 - t) * Math.pow(t, 3) + Math.pow(t, 3);
            HotUpdateMainThread.rate.put(year, rate);
        }
    }

    private static double solve(int a, int b, int c, int d) {
        double A, B, C;
        A = Math.pow(b, 2) - 3 * a * c;
        B = b * c - 9 * a * d;
        C = Math.pow(c, 2) - 3 * b * d;

        double judge = Math.pow(B, 2) - 4 * A * C;

        if (A == B && A == 0) {
            return  ((double) -b )/ 3 * (a);
        } else if (judge > 0) {
            double tmp = Math.sqrt(judge);
            double Y1 = A * b + 3 * a * (-B + tmp) / 2;
            double Y2 = A * b + 3 * a * (-B - tmp) / 2;
            double y1 = Y1 < 0 ? -Math.pow(-Y1, 1/3.0) : Math.pow(Y1, 1/3.0);
            double y2 = Y2 < 0 ? -Math.pow(-Y2, 1/3.0) : Math.pow(Y2, 1/3.0);
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
