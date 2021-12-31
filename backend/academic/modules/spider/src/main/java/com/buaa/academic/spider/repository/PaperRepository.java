package com.buaa.academic.spider.repository;

import com.buaa.academic.document.entity.Paper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PaperRepository extends ElasticsearchRepository<Paper, String> {
}
