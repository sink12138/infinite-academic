package com.buaa.academic.scholar.repository;

import com.buaa.academic.document.system.Application;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AppRepository extends ElasticsearchRepository<Application, String> {
}
