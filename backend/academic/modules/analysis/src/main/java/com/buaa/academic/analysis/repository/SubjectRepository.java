package com.buaa.academic.analysis.repository;

import com.buaa.academic.analysis.model.Subject;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SubjectRepository extends ElasticsearchRepository<Subject, String> {
}
