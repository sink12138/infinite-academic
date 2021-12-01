package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.statistic.Topic;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

public abstract class TopicDao {
    public static Topic getTopicByName(String name, ElasticsearchRestTemplate template) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery().must(
                        QueryBuilders.termQuery("name.raw", name)))
                .build();
        SearchHit<Topic> searchHit =  template.searchOne(query, Topic.class);
        if (searchHit == null)
            return null;
        return searchHit.getContent();
    }


}
