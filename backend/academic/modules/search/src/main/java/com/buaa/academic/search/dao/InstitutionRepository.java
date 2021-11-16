package com.buaa.academic.search.dao;

import com.buaa.academic.document.entity.Institution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface InstitutionRepository extends ElasticsearchRepository<Institution, String> {

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Institution> findByNameLike(String name, Pageable pageable);

}