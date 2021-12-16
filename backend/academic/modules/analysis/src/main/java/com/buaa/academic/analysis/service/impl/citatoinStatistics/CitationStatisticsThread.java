package com.buaa.academic.analysis.service.impl.citatoinStatistics;

import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CitationStatisticsThread implements Runnable{
    private ElasticsearchRestTemplate template;
    private String name;

    @Override
    public void run() {
        StatusCtrl.changeRunningStatusTo("Start citation statistics of researcher.", name);
        long startTime = System.currentTimeMillis();

        try {
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(PageRequest.of(0, 1000))
                    .build();
            SearchScrollHits<Researcher> hits = template.searchScrollStart(20000, searchQuery, Researcher.class, IndexCoordinates.of("researcher"));
            String scrollId = hits.getScrollId();
            int total = 0;
            do {
                total += hits.getSearchHits().size();
                log.info("Scrolled researchers: {}", total);

                if (StatusCtrl.isStopped(name)) {
                    StatusCtrl.changeRunningStatusToStop("Stopped.", name);
                    return;
                }

                for (SearchHit<Researcher> searchHit : hits) {
                    Researcher researcher = searchHit.getContent();
                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                            .should(QueryBuilders.termQuery("authors.id", researcher.getId()));
                    if (researcher.getCurrentInst() != null)
                        boolQueryBuilder.should(QueryBuilders.termQuery("institutions.id", researcher.getCurrentInst().getId()));
                    NativeSearchQuery query = new NativeSearchQueryBuilder()
                            .withQuery(boolQueryBuilder)
                            .addAggregation(AggregationBuilders.sum("sum").field("citationNum"))
                            .withMaxResults(0)
                            .build();
                    SearchHits<Paper> paperSearchHits = template.search(query, Paper.class);
                    int citationNum = (int) ((Sum) Objects.requireNonNull(paperSearchHits.getAggregations()).get("sum")).getValue();
                    researcher.setCitationNum(citationNum);
                    template.save(researcher);
                }
                hits = template.searchScrollContinue(scrollId, 20000, Researcher.class, IndexCoordinates.of("researcher"));
            } while (hits.hasSearchHits());
        } catch (Exception e) {
            e.printStackTrace();
            StatusCtrl.changeRunningStatusToStop("Stopped for exception.", name);
            return;
        }

        double costTime = ((double) (System.currentTimeMillis() - startTime) / 1000);
        StatusCtrl.changeRunningStatusToStop("All done! Cost " + costTime + "s.", name);
    }
}
