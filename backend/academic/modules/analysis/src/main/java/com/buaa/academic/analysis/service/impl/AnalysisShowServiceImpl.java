package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.SearchAggregation;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.Paper;
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
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpSession;

@Service
public class AnalysisShowServiceImpl implements AnalysisShowService {
    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public SearchAggregation searchAggregating(HttpSession session) {
        String filterStr = (String) session.getAttribute("filter");
        String queryStr = (String) session.getAttribute("query");

        TermsAggregationBuilder termsAgg = aggBuilderGen("authors", "authorTerm", "authorCount");

        NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(queryStr))
                .withFilter(QueryBuilders.wrapperQuery(filterStr))
                .addAggregation(termsAgg)
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
        return null;
    }

    private TermsAggregationBuilder aggBuilderGen(String target, String aggName, String countName) {
        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder(countName).field(target);
        return new TermsAggregationBuilder(aggName).field(target).subAggregation(count);
    }
}
