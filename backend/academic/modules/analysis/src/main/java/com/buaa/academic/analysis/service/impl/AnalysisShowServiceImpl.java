package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.analysis.repository.InstitutionRepository;
import com.buaa.academic.analysis.repository.JournalRepository;
import com.buaa.academic.analysis.repository.ResearcherRepository;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import com.buaa.academic.document.statistic.DataPerYear;
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
    public SearchAggregation searchAggregating(HttpSession session) {
        SearchAggregation searchAggregation = new SearchAggregation();
        ArrayList<SearchAggregation.aggregationTerm> aggregationTerms = new ArrayList<>();

        String filterStr = (String) session.getAttribute("filter");
        String queryStr = (String) session.getAttribute("query");
        String index = (String) session.getAttribute("index");

        Aggregations aggregations;
        List<Agg.AggInfo> aggInfos;
        switch (index) {
            case "paper": {
                NativeSearchQueryBuilder nativeSearchQueryBuilder = new  NativeSearchQueryBuilder()
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
                break;
            }
            case "patent": {
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
                break;
            }
            case "researcher": {
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
                break;
            }
            default:
                return null;
        }

        for (Agg.AggInfo agg: aggInfos) {

            SearchAggregation.aggregationTerm aggregationTerm = new SearchAggregation.aggregationTerm();
            aggregationTerm.setTerm(agg.target);
            ArrayList<SearchAggregation.item> items = new ArrayList<>();

            assert aggregations != null;
            Aggregation aggregation = aggregations.asMap().get(agg.aggName);

            ParsedStringTerms terms = (ParsedStringTerms) aggregation;
            for (Terms.Bucket bucket : terms.getBuckets()) {
                items.add(new SearchAggregation.item(bucket.getKey().toString(), (int) bucket.getDocCount()));
            }

            aggregationTerm.setItems(items);
            aggregationTerms.add(aggregationTerm);
        }

        searchAggregation.setAggregationTerms(aggregationTerms);
        return searchAggregation;
    }

    @Override
    public ArrayList<SimpleTopic> getHotTopics() {
        FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("heat").order(SortOrder.DESC);
        PageRequest page = PageRequest.of(0, 10);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withSort(fieldSortBuilder)
                .withFields("name", "heat")
                .withPageable(page)
                .build();
        SearchHits<Topic> searchHits = template.search(nativeSearchQuery, Topic.class);
        ArrayList<SimpleTopic> topics = new ArrayList<>();
        searchHits.forEach(topicSearchHit ->
                topics.add(new SimpleTopic(topicSearchHit.getContent().getName(), topicSearchHit.getContent().getHeat())));
        return topics;
    }

    @Override
    public ArrayList<SimpleSubject> getHotSubjects() {
        FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("heat").order(SortOrder.DESC);
        PageRequest page = PageRequest.of(0, 10);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withSort(fieldSortBuilder)
                .withFields("name", "heat")
                .withPageable(page)
                .build();
        SearchHits<Subject> searchHits = template.search(nativeSearchQuery, Subject.class);
        ArrayList<SimpleSubject> subjects = new ArrayList<>();
        searchHits.forEach(subjectSearchHit ->
                subjects.add(new SimpleSubject(subjectSearchHit.getContent().getName(), subjectSearchHit.getContent().getHeat())));
        return subjects;
    }

    @Override
    public ArrayList<DataPerYear> getPublicationStatic(String type, String id) {
        Aggregation aggregation = aggFunc(type, id, "year", 200);
        if (aggregation == null)
            return null;
        ParsedLongTerms terms = (ParsedLongTerms) aggregation;
        ArrayList<DataPerYear> data = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            data.add(new DataPerYear(Integer.parseInt(bucket.getKey().toString()), (int) bucket.getDocCount()));
        }
        return data;
    }

    @Override
    public ArrayList<WordFrequency> topicFrequencyStatic(String type, String id) {
        Aggregation aggregation = aggFunc(type, id, "topics.raw", 10);
        if (aggregation == null)
            return null;
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;
        ArrayList<WordFrequency> wordFrequencies = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            wordFrequencies.add(new WordFrequency(bucket.getKey().toString(), (int) bucket.getDocCount()));
        }
        return wordFrequencies;
    }

    @Override
    public ArrayList<Cooperation> getCooperativeRelations(String type, String id) {
        String  searchField;
        if (type.equals("institution")) {
            searchField = "institutions.id";
        } else {
            searchField = "authors.id";
        }
        Aggregation aggregation = aggFunc(type, id, searchField, 20);
        if (aggregation == null)
            return null;
        ParsedStringTerms terms = (ParsedStringTerms) aggregation;
        ArrayList<Cooperation> cooperation = new ArrayList<>();
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
                    cooperation.add(new Cooperation(aggId, name, (int) bucket.getDocCount()));
            }
        }
        return cooperation;
    }

    @Override
    public Boolean checkTargetExist(String type, String id) {
        Object target;
        switch (type) {
            case "researcher": {
                target= researcherRepository.findResearcherById(id);
                break;
            }
            case "institution": {
                target = institutionRepository.findInstitutionById(id);
                break;
            }
            case "journal": {
                target = journalRepository.findJournalById(id);
                break;
            }
            default:
                return null;
        }
        return target != null;
    }

    private Aggregation aggFunc(String type, String id, String field, Integer size) {
        QueryBuilder queryBuilder = buildQueryByType(type, id);
        if (queryBuilder == null)
            return null;
        ValueCountAggregationBuilder valueCountAggregationBuilder = new ValueCountAggregationBuilder("count").field(field);
        TermsAggregationBuilder termsAggregationBuilder  =  new TermsAggregationBuilder("term").field(field)
                .subAggregation(valueCountAggregationBuilder)
                .order(BucketOrder.count(false))
                .size(size);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .addAggregation(termsAggregationBuilder)
                .withQuery(queryBuilder)
                .build();
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
}
