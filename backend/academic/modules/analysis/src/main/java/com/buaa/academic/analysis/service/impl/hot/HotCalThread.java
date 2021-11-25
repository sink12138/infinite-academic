package com.buaa.academic.analysis.service.impl.hot;

import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.entity.Paper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

public class HotCalThread implements Runnable{
    private ElasticsearchRestTemplate template;
    private Integer partition;

    public HotCalThread(ElasticsearchRestTemplate template, Integer partition) {
        this.template = template;
        this.partition = partition;
    }

    @Override
    public void run() {
        SearchScrollHits<Paper> hits;
        while (true) {
            synchronized (StatusCtrl.STATUS_LOCK) {
                if (!HotUpdateMainThread.scrollId.isEmpty()) {
                    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.matchAllQuery())
                            .withFields("id")
                            .withPageable(PageRequest.of(0, partition))
                            .build();
                    hits = template.searchScrollStart(3000, searchQuery, Paper.class, IndexCoordinates.of("paper"));
                    HotUpdateMainThread.scrollId = hits.getScrollId();
                } else {
                    hits = template.searchScrollContinue(HotUpdateMainThread.scrollId, 3000, Paper.class, IndexCoordinates.of("paper"));
                }
                if (!hits.hasSearchHits())
                    return;
            }
            ValueCountAggregationBuilder count = new ValueCountAggregationBuilder("year");
            hits.forEach(paperSearchHit -> {
                NativeSearchQuery aggregationSearch = new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termQuery("references", paperSearchHit.getContent()))
                        .addAggregation(count)
                        .build();
            });
        }
    }
}
