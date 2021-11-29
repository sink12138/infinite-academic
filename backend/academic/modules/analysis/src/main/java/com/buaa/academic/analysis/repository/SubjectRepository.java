package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.statistic.Subject;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SubjectRepository extends ElasticsearchRepository<Subject, String> {
    Subject findSubjectByName(String name);
    Subject findSubjectById(String id);
}
