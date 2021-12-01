package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.repository.PaperRepository;
import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员用，更新数据分析结果", value = "/analysis/update")
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
    public Result<Object> testAgg() {

        NestedAggregationBuilder nested = AggregationBuilders.nested("author","institutions");
        nested.subAggregation(AggregationBuilders.terms("authorsCount").field("institutions.name.raw")
                .subAggregation(AggregationBuilders.reverseNested("reversed_author")));

        NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(nested)
                .build();
        SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
        Aggregations aggregations = searchHit.getAggregations();
        assert aggregations != null;
        Aggregation aggregation = aggregations.asMap().get("author");
        Aggregation aggregation1 = ((ParsedNested) aggregation).getAggregations().asMap().get("authorsCount");

        ParsedStringTerms terms = (ParsedStringTerms) aggregation1;
        for (Terms.Bucket bucket: terms.getBuckets()) {
            ParsedReverseNested reversed = (ParsedReverseNested) bucket.getAggregations().asMap().get("reversed_author");
            System.out.println(bucket.getKey().toString() + ":" + reversed.getDocCount());
        }
        return new Result<>();
    }

    @Autowired
    PaperRepository paperRepository;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Paper request) {
        paperRepository.save(request);
        return new Result<>();
    }

}
