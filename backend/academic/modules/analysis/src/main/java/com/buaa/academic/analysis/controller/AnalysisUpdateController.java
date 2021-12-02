package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.repository.PaperRepository;
import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "开始数据分析", notes = "先后进行关联分析和热度分析，每个分析部分话题和学科会分两个线程同时执行")
    @PostMapping("/start")
    public Result<Object> startAnalysis() {
        boolean notStarted = analysisUpdateService.start();

        if(notStarted)
            return new Result<>();
        else
            return new Result<>().withFailure("Analysis program has been running...");
    }

    @ApiOperation(value = "查看当前分析作业执行情况")
    @GetMapping("/status")
    public Result<Schedule> checkAnalysisStatus() {
        return new Result<Schedule>().withData(analysisUpdateService.getStatus());
    }

    @ApiOperation(value = "停止当前数据分析作业", notes = "为保证数据库中数据的一致性，数据分析在写入ES阶段不允许中途停止")
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
