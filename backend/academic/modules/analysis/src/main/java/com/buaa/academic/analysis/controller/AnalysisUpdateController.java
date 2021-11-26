package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.repository.PaperRepository;
import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.model.web.Result;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/update")
@RestController()
public class AnalysisUpdateController {

    @Autowired
    private AnalysisUpdateService analysisUpdateService;

    @PostMapping("/start")
    public Result<Object> startAnalysis() {
        boolean notStarted = analysisUpdateService.start();

        if(notStarted)
            return new Result<>();
        else
            return new Result<>().withFailure("Analysis program has been running...");
    }

    @GetMapping("/status")
    public Result<Object> checkAnalysisStatus() {
        return new Result<>().withData(analysisUpdateService.getStatus());
    }

    @PostMapping("/stop")
    public Result<Void> stopAnalysis() {
        analysisUpdateService.stop();
        return new Result<>();
    }

    @Autowired
    ElasticsearchRestTemplate template;

    @GetMapping("/test")
    public Result<Object> testAgg(@RequestParam(value = "topic") String topic) {
        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder("test").field("year");
        TermsAggregationBuilder termsAgg = new TermsAggregationBuilder("term").field("year").subAggregation(count);
        TermsAggregationBuilder topicTerm = new TermsAggregationBuilder("topicTerm").field("topics.raw").subAggregation(termsAgg);
        NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(topicTerm)
                .build();
        SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
        Aggregations aggregations = searchHit.getAggregations();
        assert aggregations != null;
        Aggregation aggregation = aggregations.asMap().get("topicTerm");
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;
        for (Terms.Bucket bucket: terms.getBuckets()) {
            System.out.println(bucket.getKey().toString());
            Aggregation aggregationYear = bucket.getAggregations().get("term");
            ParsedLongTerms longTerms = (ParsedLongTerms) aggregationYear;
            for (Terms.Bucket bucket1: longTerms.getBuckets()) {
                System.out.println(bucket1.getKey().toString() + ": " + bucket1.getDocCount());
            }
        }
        return new Result<>();
    }

    @Autowired
    PaperRepository paperRepository;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Paper request) {
        //template.indexOps(Paper.class).putMapping();
        paperRepository.save(request);
        return new Result<>();
    }

}
