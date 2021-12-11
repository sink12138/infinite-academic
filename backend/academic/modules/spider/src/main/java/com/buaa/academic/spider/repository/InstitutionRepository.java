package com.buaa.academic.spider.repository;

import com.buaa.academic.document.entity.Institution;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface InstitutionRepository extends ElasticsearchRepository<Institution, String> {
}
