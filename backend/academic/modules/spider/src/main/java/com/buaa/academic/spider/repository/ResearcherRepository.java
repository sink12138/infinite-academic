package com.buaa.academic.spider.repository;

import com.buaa.academic.document.entity.Researcher;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ResearcherRepository extends ElasticsearchRepository<Researcher, String> {
}
