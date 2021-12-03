package com.buaa.academic.analysis.dao;

import com.buaa.academic.document.entity.Paper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperRepository extends ElasticsearchRepository<Paper, String>, CustomRepository {
}
