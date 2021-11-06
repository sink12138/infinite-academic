package com.buaa.academic.search.dao;

import com.buaa.academic.document.entity.Researcher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearcherRepository extends ElasticsearchRepository<Researcher, String> {

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Researcher> findByNameEquals(String name, Pageable pageable);

}
