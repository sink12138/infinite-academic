package com.buaa.academic.search.service.impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.search.service.SearchService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Value("${spring.elasticsearch.highlight.pre-tag")
    private String preTag;

    @Value("${spring.elasticsearch.highlight.post-tag")
    private String postTag;

    @Override
    public <T> T searchById(Class<T> type, String id) {
        return template.get(id, type);
    }

    @Override
    public <T> SearchHits<T> advancedSearch(Class<T> target, QueryBuilder query, QueryBuilder filter, SortBuilder<?> sort, HighlightBuilder hlt, Pageable page) {
        return template.search(
                new NativeSearchQueryBuilder()
                        .withQuery(query)
                        .withFilter(filter)
                        .withSort(sort)
                        .withHighlightBuilder(hlt)
                        .withPageable(page)
                        .build(),
                target);
    }

    @Override
    public SearchHits<Paper> smartSearch(String[] keywords, QueryBuilder filter, SortBuilder<?> sort, Pageable page) {
        QueryBuilder query;
        String[] matchFields = new String[] { "title", "keywords", "abstract", "authors.name" };
        String[] hltFields = new String[] { "title", "keywords", "abstract" };
        if (keywords.length > 1) {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (String keyword : keywords) {
                boolQuery.should(QueryBuilders.multiMatchQuery(keyword, matchFields));
            }
            query = boolQuery;
        }
        else {
            query = QueryBuilders.multiMatchQuery(keywords[0], matchFields);
        }
        HighlightBuilder hlt = new HighlightBuilder();
        for (String field : hltFields) {
            hlt.field(field);
        }
        hlt.preTags(preTag).postTags(postTag);
        return advancedSearch(Paper.class, query, filter, sort, hlt, page);
    }

}
