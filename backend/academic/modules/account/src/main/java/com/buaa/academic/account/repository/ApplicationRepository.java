package com.buaa.academic.account.repository;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
public interface ApplicationRepository extends ElasticsearchRepository<Application,String> {

    Page<Application> findByUserIdEqualsAndStatusEqualsOrderByTimeDesc(String userId, String status, Pageable pageable);


    Page<Application> findByUserIdEqualsOrderByTimeDesc(String userId, Pageable pageable);


    Page<Application> findByUserIdEqualsAndTypeEqualsOrderByTimeDesc(String userId, String type, Pageable pageable);

    Page<Application> findByUserIdEqualsAndStatusEqualsAndTypeEqualsOrderByTimeDesc(String userId, String status, String type, Pageable pageable);
}
