package com.buaa.academic.spider.repository;

import com.buaa.academic.document.entity.Journal;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface JournalRepository extends ElasticsearchRepository<Journal, String> {
}
