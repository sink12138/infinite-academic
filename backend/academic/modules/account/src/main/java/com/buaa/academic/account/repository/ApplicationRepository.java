package com.buaa.academic.account.repository;

import com.buaa.academic.document.system.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
public interface ApplicationRepository extends ElasticsearchRepository<Application,String> {

    Page<Application> findByUserId(String userId, Pageable pageable);

    Page<Application> findByUserIdAndType(String userId, String type, Pageable pageable);

    Page<Application> findByUserIdAndStatus(String userId, String status, Pageable pageable);

    Page<Application> findByUserIdAndStatusAndType(String userId, String status, String type, Pageable pageable);

}
