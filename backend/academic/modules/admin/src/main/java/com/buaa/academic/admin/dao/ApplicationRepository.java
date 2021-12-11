package com.buaa.academic.admin.dao;

import com.buaa.academic.document.system.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends ElasticsearchRepository<Application, String> {

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Page<Application> findByType(String type, Pageable pageable);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Page<Application> findByStatus(String status, Pageable pageable);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Page<Application> findByTypeAndStatus(String type, String status, Pageable pageable);

}
