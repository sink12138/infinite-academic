package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.statistic.Topic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends ElasticsearchRepository<Topic, String> {
}
