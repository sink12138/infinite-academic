package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.entity.Researcher;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearcherRepository extends ElasticsearchRepository<Researcher, String> {
    String pattern = "{\"constant_score\": {" +
            "      \"filter\": {" +
            "        \"term\": {" +
            "          \"_id\": \"?0\"" +
            "        }" +
            "      }" +
            "    }" +
            "}";
    @Query(pattern)
    Researcher findResearcherById(String id);
}
