package com.buaa.academic.scholar.repository;

import com.buaa.academic.document.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {

    boolean existsByResearcherId(String researcherId);

}
