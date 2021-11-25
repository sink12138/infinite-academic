package com.buaa.academic.search.service;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.search.model.request.Condition;
import com.buaa.academic.search.model.request.Filter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

public interface SearchService {

    <T> SearchHits<T> advancedSearch(Class<T> target, QueryBuilder query, QueryBuilder filter, SortBuilder<?> sort, HighlightBuilder hlt, Pageable page);

    SearchHits<Paper> smartSearch(String[] keywords, QueryBuilder filter, SortBuilder<?> sort, Pageable page);

    default QueryBuilder buildQuery(List<Condition> conditions, String strategy) {
        QueryBuilder query;
        if (conditions.size() == 1) {
            query = conditions.get(0).compile(strategy);
        } else {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (Condition condition : conditions) {
                switch (condition.getLogic()) {
                    case "and" -> boolQuery.must(condition.compile(strategy));
                    case "or" -> boolQuery.should(condition.compile(strategy));
                    case "not" -> boolQuery.mustNot(condition.compile(strategy));
                }
            }
            query = boolQuery;
        }
        return query;
    }

    default QueryBuilder buildFilter(List<Filter> filters) {
        BoolQueryBuilder filter = null;
        if (!filters.isEmpty()) {
            filter = QueryBuilders.boolQuery();
            for (Filter flt : filters) {
                filter.must(flt.compile());
            }
        }
        return filter;
    }

    default SortBuilder<?> buildSort(String srt) {
        SortBuilder<?> sort;
        if (srt == null)
            sort = SortBuilders.scoreSort();
        else {
            String[] srtParams = srt.split("\\.");
            sort = SortBuilders.fieldSort(srtParams[0]);
            if (srtParams[1].equals("asc"))
                sort.order(SortOrder.ASC);
            else
                sort.order(SortOrder.DESC);
        }
        return sort;
    }

    default HighlightBuilder buildHighlight(String ... fields) {
        HighlightBuilder hlt = new HighlightBuilder();
        for (String field : fields) {
            hlt.field(field);
        }
        return hlt;
    }

}
