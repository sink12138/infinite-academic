package com.buaa.academic.analysis.service.impl.citatoinStatistics;

import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
            SearchScrollHits<Researcher> hits = template.searchScrollStart(600000, searchQuery, Researcher.class, IndexCoordinates.of("researcher"));
            String scrollId = hits.getScrollId();
            int total = 0;
            do {
                total += hits.getSearchHits().size();
                log.info("batch size: {}", total);

                if (StatusCtrl.isStopped(name)) {
                    StatusCtrl.changeRunningStatusToStop("Stopped. ", name);
                    return;
                }

                List<SearchHit<Researcher>> searchHits = hits.getSearchHits();

                for (SearchHit<Researcher> searchHit : searchHits) {
                    Researcher researcher = searchHit.getContent();
                    NativeSearchQuery query = new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.termQuery("authors.name", researcher.getName()))
                            .withFields("citationNum")
                            .build();
                    SearchHits<Paper> paperSearchHits = template.search(query, Paper.class);
                    AtomicInteger num = new AtomicInteger();
                    paperSearchHits.forEach(hit -> {
                        num.addAndGet(hit.getContent().getCitationNum());
                    });
                    researcher.setCitationNum(num.intValue());
                    template.save(researcher);
                }
                hits = template.searchScrollContinue(scrollId, 600000, Researcher.class, IndexCoordinates.of("researcher"));
            } while (hits.hasSearchHits());
        } catch (Exception e) {
            e.printStackTrace();
            StatusCtrl.changeRunningStatusToStop("Stopped for exception.", name);
            return;
        }

        double costTime = ((double) (System.currentTimeMillis() - startTime) / 1000);
        StatusCtrl.changeRunningStatusToStop("All done! " + "Cost " + costTime + "s. ", name);
    }
}
