package com.buaa.academic.search.service.impl;

import com.buaa.academic.search.config.HighlightConfiguration;
import com.buaa.academic.search.model.request.Condition;
import com.buaa.academic.search.model.request.Filter;
import com.buaa.academic.search.service.SearchService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private HighlightConfiguration config;

    @Override
    public <T> SearchHits<T> runSearch(Class<T> target, QueryBuilder query, QueryBuilder filter, SortBuilder<?> sort, HighlightBuilder hlt, Pageable page) {
        return template.search(
                new NativeSearchQueryBuilder()
                        .withQuery(query)
                        .withFilter(filter)
                        .withSort(sort)
                        .withHighlightBuilder(hlt)
                        .withPageable(page)
                        .withTrackTotalHits(page.getPageNumber() == 0)
                        .build(),
                target);
    }

    @Override
    public QueryBuilder buildQuery(List<Condition> conditions, String strategy) {
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

    @Override
    public QueryBuilder buildFilter(List<Filter> filters, String strategy) {
        BoolQueryBuilder filter = null;
        if (!filters.isEmpty()) {
            filter = QueryBuilders.boolQuery();
            for (Filter flt : filters) {
                filter.must(flt.compile(strategy));
            }
        }
        return filter;
    }

    @Override
    public SortBuilder<?> buildSort(String srt) {
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

    @Override
    public HighlightBuilder buildHighlight(String ... fields) {
        HighlightBuilder hlt = new HighlightBuilder();
        for (String field : fields) {
            hlt.field(field);
        }
        return hlt.preTags(config.preTag()).postTags(config.postTag());
    }

}
