package com.buaa.academic.search.dao;

import com.buaa.academic.document.entity.Patent;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatentRepository extends ElasticsearchRepository<Patent, String> {

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Patent> findByTitleLike(String title);

    SearchPage<Patent> search(Query query);

}
