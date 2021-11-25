package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.service.AnalysisService;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.model.web.Result;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/update")
public class AnalysisUpdateController {

    @Autowired
    private AnalysisService analysisService;

    @PostMapping("/start")
    public Result<Object> startAnalysis() {
        boolean notStarted = analysisService.start();

        if(notStarted)
            return new Result<>();
        else
            return new Result<>().withFailure("Analysis program has been running...");
    }

    @GetMapping("/status")
    public Result<Object> checkAnalysisStatus() {
        return new Result<>().withData(analysisService.getStatus());
    }

    @PostMapping("/stop")
    public Result<Void> stopAnalysis() {
        analysisService.stop();
        return new Result<>();
    }

    @Autowired
    ElasticsearchRestTemplate template;

    @GetMapping("/test")
    public Result<Object> testAgg(@Param(value = "id") String id) {
        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder("test").field("year");
        NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("references", id))
                .addAggregation(count)
                .build();
        SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
        Aggregations aggregations = searchHit.getAggregations();
        return new Result<>();
    }

}
