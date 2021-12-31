package com.buaa.academic.analysis.dao.impl;

import com.buaa.academic.analysis.dao.CustomRepository;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class CustomRepositoryImpl implements CustomRepository {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public Subject findSubjectByName(String name) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("name.raw", name))
                .build();
        SearchHit<Subject> searchHit =  template.searchOne(query, Subject.class);
        if (searchHit == null)
            return null;
        return searchHit.getContent();
    }

    @Override
    public Topic findTopicByName(String name) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("name.raw", name))
                .build();
        SearchHit<Topic> searchHit =  template.searchOne(query, Topic.class);
        if (searchHit == null)
            return null;
        return searchHit.getContent();
    }

}
