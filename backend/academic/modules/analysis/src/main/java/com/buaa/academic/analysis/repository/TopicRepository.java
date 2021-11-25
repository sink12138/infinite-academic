package com.buaa.academic.analysis.repository;

import com.buaa.academic.analysis.model.Topic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TopicRepository extends ElasticsearchRepository<Topic, String> {
    Topic findTopicByName(String name);
}
