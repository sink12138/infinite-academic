package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.statistic.Subject;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends ElasticsearchRepository<Subject, String> {
}
