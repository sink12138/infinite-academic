package com.buaa.academic.search.service.impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
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
    public SearchHits<Paper> smartSearchForPapers(String keyword, Pageable pageable) {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchQuery("title", keyword))
                        .should(QueryBuilders.matchQuery("keywords", keyword))
                        .should(QueryBuilders.matchQuery("abstract", keyword))
                        .should(QueryBuilders.termQuery("authors.name", keyword)))
                .withPageable(pageable)
                // .withSort(SortBuilders.fieldSort("year").order(SortOrder.DESC))
                .withHighlightBuilder(new HighlightBuilder()
                        .field("title")
                        .field("keywords")
                        .field("abstract")
                        .field("authors.name")
                        .preTags(preTag)
                        .postTags(postTag))
                .withTrackTotalHits(true)
                .build();
        return template.search(query, Paper.class);
    }

}
