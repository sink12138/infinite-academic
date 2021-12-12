package com.buaa.academic.analysis.service.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import java.util.List;

public abstract class Agg {
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AggInfo {
        String field;
        String aggName;
        String countName;
    }

    public static final TermsAggregationBuilder authorAgg =  aggBuilderGen("authors.name", "authorTerm", "authorCount");
    public static final  TermsAggregationBuilder institutionAgg = aggBuilderGen("institutions.name.raw", "institutionTerm", "institutionCount");
    public static final TermsAggregationBuilder journalAgg = aggBuilderGen("journal.title.raw", "journalTerm", "journalCount");
    public static final TermsAggregationBuilder subjectAgg = aggBuilderGen("subjects.raw", "subjectTerm", "subjectCount");
    public static final TermsAggregationBuilder typeAgg = aggBuilderGen("type", "typeTerm", "typeCount");
    public static final TermsAggregationBuilder keywordAgg = aggBuilderGen("keywords.raw", "keywordTerm", "keywordCount");
    public static final List<AggInfo> paperAgg = List.of(
            new AggInfo("authors", "authorTerm", "authorCount"),
            new AggInfo("subjects", "subjectTerm", "subjectCount"),
            new AggInfo("institutions", "institutionTerm", "institutionCount"),
            new AggInfo("journals", "journalTerm", "journalCount"),
            new AggInfo("types", "typeTerm", "typeCount"),
            new AggInfo("keywords", "keywordTerm", "keywordCount")
    );

    public static final TermsAggregationBuilder interestAgg = aggBuilderGen("interests.raw", "interestTerm", "interestCount");
    public static final TermsAggregationBuilder currentInstAgg = aggBuilderGen("currentInst.name.raw", "currentInstTerm", "currentInstCount");
    public static final List<AggInfo> researcherAgg = List.of (
            new AggInfo("interests", "interestTerm", "interestCount"),
            new AggInfo("institutions", "currentInstTerm", "currentInstCount")
    );

    public static final TermsAggregationBuilder inventorAgg = aggBuilderGen("inventors.name", "inventorTerm", "inventorCount");
    public static final TermsAggregationBuilder applicantAgg = aggBuilderGen("applicant.raw", "applicantTerm", "applicantCount");
    public static final List<AggInfo> patentAgg = List.of(
            new AggInfo("types", "typeTerm", "typeCount"),
            new AggInfo("inventors", "inventorTerm", "inventorCount"),
            new AggInfo("applicants", "applicantTerm", "applicantCount")
    );

    private static TermsAggregationBuilder aggBuilderGen(String field, String aggName, String countName) {
        ValueCountAggregationBuilder count = new ValueCountAggregationBuilder(countName).field(field);
        return new TermsAggregationBuilder(aggName).field(field).subAggregation(count);
    }
}
