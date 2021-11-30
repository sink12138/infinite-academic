package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.SearchAggregation;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.document.entity.Researcher;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisShowServiceImpl implements AnalysisShowService {
    @Autowired
    private ElasticsearchRestTemplate template;

    @AllArgsConstructor
    @NoArgsConstructor
    private static class AggInfo {
        String target;
        String aggName;
        String countName;
    }

    private static final TermsAggregationBuilder authorAgg = aggBuilderGen("authors", "authorTerm", "authorCount");
    private static final TermsAggregationBuilder subjectAgg = aggBuilderGen("subjects.raw", "subjectTerm", "subjectCount");
    private static final TermsAggregationBuilder topicAgg = aggBuilderGen("topics.raw", "topicTerm", "topicCount");
    private static final TermsAggregationBuilder institutionAgg = aggBuilderGen("institutions", "institutionTerm", "institutionCount");
    private static final TermsAggregationBuilder journalAgg = aggBuilderGen("journal", "journalTerm", "journalCount");
    private static final TermsAggregationBuilder typeAgg = aggBuilderGen("type", "typeTerm", "typeCount");
    private static final TermsAggregationBuilder keywordAgg = aggBuilderGen("keywords.raw", "keywordTerm", "keywordCount");
    private static final List<AggInfo> paperAgg = List.of(
            new AggInfo("author", "authorTerm", "authorCount"),
            new AggInfo("subjects", "subjectTerm", "subjectCount"),
            new AggInfo("topic", "topicTerm", "topicCount"),
            new AggInfo("institution", "institutionTerm", "institutionCount"),
            new AggInfo("journal", "journalTerm", "journalCount"),
            new AggInfo("type", "typeTerm", "typeCount"),
            new AggInfo("keywords", "keywordTerm", "keywordCount")
            );

    private static final TermsAggregationBuilder interestAgg = aggBuilderGen("interests.raw", "interestTerm", "interestCount");
    private static final TermsAggregationBuilder currentInstAgg = aggBuilderGen("currentInst", "currentInstTerm", "currentInstCount");
    private static final List<AggInfo> researcherAgg = List.of(
            new AggInfo("interests", "interestTerm", "interestCount"),
            new AggInfo("currentInst", "currentInstTerm", "currentInstCount")
    );

    private static final TermsAggregationBuilder inventorAgg = aggBuilderGen("inventors", "inventorTerm", "inventorCount");
    private static final TermsAggregationBuilder applicantAgg = aggBuilderGen("applicant", "applicantTerm", "applicantCount");
    private static final List<AggInfo> patentAgg = List.of(
            new AggInfo("type", "typeTerm", "typeCount"),
            new AggInfo("inventors", "inventorTerm", "inventorCount"),
            new AggInfo("applicant", "applicantTerm", "applicantCount")
            );


    @Override
    public SearchAggregation searchAggregating(HttpSession session) {
        SearchAggregation searchAggregation = new SearchAggregation();
        ArrayList<SearchAggregation.aggregationTerm> aggregationTerms = new ArrayList<>();

        String filterStr = (String) session.getAttribute("filter");
        String queryStr = (String) session.getAttribute("query");
        String index = (String) session.getAttribute("index");

        Aggregations aggregations;
        List<AggInfo> aggInfos;
        switch (index) {
            case "paper": {
                NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.wrapperQuery(queryStr))
                        .withFilter(QueryBuilders.wrapperQuery(filterStr))
                        .addAggregation(authorAgg)
                        .addAggregation(subjectAgg)
                        .addAggregation(topicAgg)
                        .addAggregation(institutionAgg)
                        .addAggregation(journalAgg)
                        .addAggregation(typeAgg)
                        .addAggregation(keywordAgg)
                        .build();
                SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
                aggregations = searchHit.getAggregations();
                aggInfos = paperAgg;
                break;
            }
            case "patent": {
                NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.wrapperQuery(queryStr))
                        .withFilter(QueryBuilders.wrapperQuery(filterStr))
                        .addAggregation(typeAgg)
                        .addAggregation(inventorAgg)
                        .addAggregation(applicantAgg)
                        .build();
                SearchHits<Patent> searchHit = template.search(aggregationSearch, Patent.class);
                aggregations = searchHit.getAggregations();
                aggInfos = patentAgg;
                break;
            }
            case "researcher": {
                NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.wrapperQuery(queryStr))
                        .withFilter(QueryBuilders.wrapperQuery(filterStr))
                        .addAggregation(interestAgg)
                        .addAggregation(currentInstAgg)
                        .build();
                SearchHits<Researcher> searchHit = template.search(aggregationSearch, Researcher.class);
                aggregations = searchHit.getAggregations();
                aggInfos = researcherAgg;
                break;
            }
            default:
                return null;
        }

        for (AggInfo agg: aggInfos) {

            SearchAggregation.aggregationTerm aggregationTerm = new SearchAggregation.aggregationTerm();
            aggregationTerm.setTerm(agg.target);
            ArrayList<SearchAggregation.item> items = new ArrayList<>();

            assert aggregations != null;
            Aggregation aggregation = aggregations.asMap().get(agg.aggName);
            ParsedStringTerms terms = (ParsedStringTerms) aggregation;
            for (Terms.Bucket bucket : terms.getBuckets()) {
                System.out.println(bucket.getKey().toString());
                Aggregation aggregationCount = bucket.getAggregations().get(agg.countName);
                ParsedLongTerms longTerms = (ParsedLongTerms) aggregationCount;
                for (Terms.Bucket bucket1 : longTerms.getBuckets()) {
                    items.add(new SearchAggregation.item(bucket1.getKey().toString(), (int) bucket1.getDocCount()));
                }
            }
            aggregationTerm.setItems(items);
            aggregationTerms.add(aggregationTerm);
        }

        searchAggregation.setAggregationTerms(aggregationTerms);
        return searchAggregation;
    }

    private static TermsAggregationBuilder aggBuilderGen(String field, String aggName, String countName) {
        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder(countName).field(field);
        return new TermsAggregationBuilder(aggName).field(field).subAggregation(count);
    }
}
