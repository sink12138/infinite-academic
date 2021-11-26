package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.entity.Paper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PaperRepository extends ElasticsearchRepository<Paper, String> {
}
