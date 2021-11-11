package com.buaa.academic.search.service;

import com.buaa.academic.document.entity.Paper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface SearchService {

    <T> SearchHits<T> advancedSearch(Class<T> target, QueryBuilder query, QueryBuilder filter, SortBuilder<?> sort, HighlightBuilder hlt, Pageable page);

    SearchHits<Paper> smartSearch(String[] keywords, QueryBuilder filter, SortBuilder<?> sort, Pageable page);

}
