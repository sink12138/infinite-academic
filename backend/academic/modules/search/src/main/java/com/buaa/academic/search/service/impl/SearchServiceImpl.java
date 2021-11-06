package com.buaa.academic.search.service.impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public SearchHits<Paper> smartSearchForPapers(String keyword, Pageable pageable) {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchQuery("title", keyword))
                        .should(QueryBuilders.matchQuery("keywords", keyword))
                        .should(QueryBuilders.matchQuery("abstract", keyword))
                        .should(QueryBuilders.termQuery("authors.name", keyword)))
                .withHighlightBuilder(new HighlightBuilder()
                        .field("title")
                        .field("keywords")
                        .field("abstract")
                        .field("authors.name")
                        .preTags("<b>")
                        .postTags("</b>"))
                .withTrackTotalHits(true)
                .build();
        return template.search(query, Paper.class);
    }

}
