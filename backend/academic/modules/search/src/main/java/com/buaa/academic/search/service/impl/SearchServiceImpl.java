package com.buaa.academic.search.service.impl;

import com.buaa.academic.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public <T> SearchHits<T> runSearch(Class<T> target, QueryBuilder query, QueryBuilder filter, SortBuilder<?> sort, HighlightBuilder hlt, Pageable page) {
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

}
