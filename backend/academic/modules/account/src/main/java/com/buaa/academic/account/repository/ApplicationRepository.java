package com.buaa.academic.account.repository;

import com.buaa.academic.document.system.Application;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ApplicationRepository extends ElasticsearchRepository<Application,String> {
    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Application> findByUserIdEqualsAndStatusEqualsOrderByTimeDesc(String userId, String status, Pageable pageable);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Application> findByUserIdEqualsOrderByTimeDesc(String userId, Pageable pageable);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Application> findByUserIdEqualsAndTypeEqualsOrderByTimeDesc(String userId, String type, Pageable pageable);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Application> findByUserIdEqualsAndStatusEqualsAndTypeEqualsOrderByTimeDesc(String userId, String status, String type,  Pageable pageable);
}
