package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.EntityFrequency;
import com.buaa.academic.analysis.model.Frequency;
import com.buaa.academic.analysis.model.HotWord;
import com.buaa.academic.analysis.model.SearchAggregation;
import com.buaa.academic.analysis.repository.InstitutionRepository;
import com.buaa.academic.analysis.repository.JournalRepository;
import com.buaa.academic.analysis.repository.ResearcherRepository;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.*;
import com.buaa.academic.document.statistic.SumPerYear;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    @Autowired
    private ResearcherRepository researcherRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private JournalRepository journalRepository;

    @Override
    public List<SearchAggregation> searchAggregating(HttpSession session) {
        List<SearchAggregation> searchAggregations = new ArrayList<>();

        String filterStr = (String) session.getAttribute("filter");
        String queryStr = (String) session.getAttribute("query");
        String index = (String) session.getAttribute("index");

        if (queryStr == null || index == null)
            return null;

        Aggregations aggregations;
        List<Agg.AggInfo> aggInfos;
        switch (index) {
            case "paper" -> {
                NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                        .withQuery(new WrapperQueryBuilder(queryStr))
                        .addAggregation(Agg.authorAgg)
                        .addAggregation(Agg.subjectAgg)
                        .addAggregation(Agg.topicAgg)
                        .addAggregation(Agg.institutionAgg)
                        .addAggregation(Agg.journalAgg)
                        .addAggregation(Agg.typeAgg)
                        .addAggregation(Agg.keywordAgg);
                if (filterStr != null && !filterStr.isEmpty())
                    nativeSearchQueryBuilder.withFilter(QueryBuilders.wrapperQuery(filterStr));
                NativeSearchQuery aggregationSearch = nativeSearchQueryBuilder.build();
                SearchHits<Paper> searchHit = template.search(aggregationSearch, Paper.class);
                aggregations = searchHit.getAggregations();
                aggInfos = Agg.paperAgg;
            }
            case "patent" -> {
                NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.wrapperQuery(queryStr))
                        .addAggregation(Agg.typeAgg)
                        .addAggregation(Agg.inventorAgg)
                        .addAggregation(Agg.applicantAgg);
                if (filterStr != null && !filterStr.isEmpty())
                    nativeSearchQueryBuilder.withFilter(QueryBuilders.wrapperQuery(filterStr));
                NativeSearchQuery aggregationSearch = nativeSearchQueryBuilder.build();
                SearchHits<Patent> searchHit = template.search(aggregationSearch, Patent.class);
                aggregations = searchHit.getAggregations();
                aggInfos = Agg.patentAgg;
            }
            case "researcher" -> {
                NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.wrapperQuery(queryStr))
                        .addAggregation(Agg.interestAgg)
                        .addAggregation(Agg.currentInstAgg);
                if (filterStr != null && !filterStr.isEmpty())
                    nativeSearchQueryBuilder.withFilter(QueryBuilders.wrapperQuery(filterStr));
                NativeSearchQuery aggregationSearch = nativeSearchQueryBuilder.build();
                SearchHits<Researcher> searchHit = template.search(aggregationSearch, Researcher.class);
                aggregations = searchHit.getAggregations();
                aggInfos = Agg.researcherAgg;
            }
            default -> {
                return null;
            }
        }

        for (Agg.AggInfo agg : aggInfos) {

            SearchAggregation searchAggregation = new SearchAggregation();
            searchAggregation.setField(agg.target);
            List<Frequency> items = new ArrayList<>();

            assert aggregations != null;
            Aggregation aggregation = aggregations.asMap().get(agg.aggName);

            ParsedStringTerms terms = (ParsedStringTerms) aggregation;
            for (Terms.Bucket bucket : terms.getBuckets()) {
                items.add(new Frequency(bucket.getKey().toString(), (int) bucket.getDocCount()));
            }

            searchAggregation.setBuckets(items);
            searchAggregations.add(searchAggregation);
        }

        return searchAggregations;
    }

    @Override
    public List<HotWord> getHotWords(String field, int num) {
        FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("heat").order(SortOrder.DESC);
        PageRequest page = PageRequest.of(0, num);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withSort(fieldSortBuilder)
                .withFields("name", "heat")
                .withPageable(page)
                .build();
        if (field.equals("topics")) {
            SearchHits<Topic> searchHits = template.search(nativeSearchQuery, Topic.class);
            List<HotWord> hotTopics = new ArrayList<>();
            searchHits.forEach(topicSearchHit ->
                    hotTopics.add(new HotWord(topicSearchHit.getContent().getName(), topicSearchHit.getContent().getHeat())));
            return hotTopics;
        } else {
            SearchHits<Subject> searchHits = template.search(nativeSearchQuery, Subject.class);
            List<HotWord> subjects = new ArrayList<>();
            searchHits.forEach(subjectSearchHit ->
                    subjects.add(new HotWord(subjectSearchHit.getContent().getName(), subjectSearchHit.getContent().getHeat())));
            return subjects;
        }
    }

    @Override
    public SumPerYear getPublicationStatic(String type, String id) {
        QueryBuilder queryBuilder = buildQueryByType(type, id);
        Aggregation aggregation = aggFunc(queryBuilder, "year", 200, false);
        if (aggregation == null)
            return null;
        ParsedLongTerms terms = (ParsedLongTerms) aggregation;
        SumPerYear data = new SumPerYear();
        List<Integer> years = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            years.add(Integer.parseInt(bucket.getKey().toString()));
            numbers.add((int) bucket.getDocCount());
        }
        data.setYears(years);
        data.setNums(numbers);
        return data;
    }

    @Override
    public List<Frequency> wordFrequencyStatistics(String type, String id, String field, int num) {
        QueryBuilder queryBuilder = buildQueryByType(type, id);
        Aggregation aggregation = aggFunc(queryBuilder, field + ".raw", num, true);
        return getFrequency(aggregation);
    }

    private List<Frequency> getFrequency(Aggregation aggregation) {
        if (aggregation == null)
            return null;
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;
        List<Frequency> wordFrequencies = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            wordFrequencies.add(new Frequency(bucket.getKey().toString(), (int) bucket.getDocCount()));
        }
        return wordFrequencies;
    }

    @Override
    public List<EntityFrequency> topEntities(String field, String name, String type, int num) {
        QueryBuilder queryBuilder = buildQueryByField(field, name);
        String aggField = switch (type) {
            case "researcher" -> "authors.id";
            case "institution" -> "institutions.id";
            case "journal" -> "journal.title";
            default -> null;
        };
        if (aggField == null)
            return null;
        Aggregation aggregation = aggFunc(queryBuilder, aggField, num, true);
        if (aggregation == null)
            return null;
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;
        List<EntityFrequency> topEntities = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            String aggId = bucket.getKey().toString();
            String entityName = null;
            switch (type) {
                case "institution" -> {
                    Institution institution = institutionRepository.findInstitutionById(aggId);
                    if (institution != null)
                        entityName = institution.getName();
                }
                case "researcher" -> {
                    Researcher researcher = researcherRepository.findResearcherById(aggId);
                    if (researcher != null)
                        entityName = researcher.getName();
                }
                case "journal" -> {
                    Journal journal = journalRepository.findJournalById(aggId);
                    if (journal != null)
                        entityName = journal.getTitle();
                }
            }
            if (entityName != null)
                topEntities.add(new EntityFrequency(entityName, (int) bucket.getDocCount(), aggId));
        }
        return topEntities;
    }

    @Override
    public List<EntityFrequency> getCooperativeRelations(String type, String id, int num) {
        String searchField;
        if (type.equals("institution")) {
            searchField = "institutions.id";
        } else {
            searchField = "authors.id";
        }
        QueryBuilder queryBuilder = buildQueryByType(type, id);
        Aggregation aggregation = aggFunc(queryBuilder, searchField, num, true);
        if (aggregation == null)
            return null;
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;
        List<EntityFrequency> cooperation = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            if (!id.equals(bucket.getKey().toString())) {
                String aggId = bucket.getKey().toString();
                String name = null;
                if (type.equals("institution")) {
                    Institution institution = institutionRepository.findInstitutionById(aggId);
                    if (institution != null)
                        name = institution.getName();
                } else {
                    Researcher researcher = researcherRepository.findResearcherById(aggId);
                    if (researcher != null)
                        name = researcher.getName();
                }
                if (name != null)
                    cooperation.add(new EntityFrequency(name, (int) bucket.getDocCount(), aggId));
            }
        }
        return cooperation;
    }

    @Override
    public boolean existsTarget(Class<?> target, String id) {
        return template.exists(id, target);
    }

    private Aggregation aggFunc(QueryBuilder queryBuilder, String field, Integer size, Boolean bucketCountSort) {
        if (queryBuilder == null)
            return null;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder("count").field(field);
        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder("term").field(field)
                .subAggregation(valueCountAggregationBuilder)
                .size(size);
        if (bucketCountSort)
            termsAggregationBuilder.order(BucketOrder.count(false));
        else
            termsAggregationBuilder.order(BucketOrder.key(true));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .addAggregation(termsAggregationBuilder)
                .withQuery(queryBuilder);
        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();
        SearchHits<Paper> searchHits = template.search(nativeSearchQuery, Paper.class);
        Aggregations aggregations = searchHits.getAggregations();
        assert aggregations != null;
        return aggregations.asMap().get("term");
    }

    private QueryBuilder buildQueryByType(String type, String id) {
        QueryBuilder queryBuilder;
        switch (type) {
            case "researcher": {
                queryBuilder = QueryBuilders.termQuery("authors.id", id);
                break;
            }
            case "institution": {
                queryBuilder = QueryBuilders.termQuery("institutions.id", id);
                break;
            }
            case "journal": {
                queryBuilder = QueryBuilders.termQuery("journal.id", id);
                break;
            }
            default:
                return null;
        }
        return queryBuilder;
    }

    private QueryBuilder buildQueryByField(String field, String name) {
        QueryBuilder queryBuilder;
        if (field.equals("topic"))
            queryBuilder = QueryBuilders.termQuery("topics.raw", name);
        else
            queryBuilder = QueryBuilders.termQuery("subjects.raw", name);
        return queryBuilder;
    }
}
