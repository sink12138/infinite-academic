package com.buaa.academic.search.dao;

import com.buaa.academic.document.entity.Paper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperRepository extends ElasticsearchRepository<Paper, String> {

    Paper findTopByDoi(String doi);

    Paper findTopByIssn(String issn);

    Paper findTopByIsbn(String isbn);

}
