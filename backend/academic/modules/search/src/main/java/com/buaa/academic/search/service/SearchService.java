package com.buaa.academic.search.service;

import com.buaa.academic.search.model.request.Condition;
import com.buaa.academic.search.model.request.Filter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

public interface SearchService {

    <T> SearchHits<T> runSearch(Class<T> target, QueryBuilder query, QueryBuilder filter, SortBuilder<?> sort, HighlightBuilder hlt, Pageable page);

    QueryBuilder buildQuery(List<Condition> conditions, String strategy);

    QueryBuilder buildFilter(List<Filter> filters, String strategy);

    SortBuilder<?> buildSort(String srt);

    HighlightBuilder buildHighlight(String ... fields);

}
