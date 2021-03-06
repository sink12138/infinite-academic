package com.buaa.academic.search.dao;

import com.buaa.academic.document.entity.Journal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalRepository extends ElasticsearchRepository<Journal, String> {

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Journal> findByTitleMatches(String title, Pageable pageable);

    Journal findTopByIssn(String issn);

}
