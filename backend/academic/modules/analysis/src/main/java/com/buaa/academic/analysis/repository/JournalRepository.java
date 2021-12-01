package com.buaa.academic.analysis.repository;

import com.buaa.academic.document.entity.Journal;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface JournalRepository extends ElasticsearchRepository<Journal, String> {
    String pattern = "{\"constant_score\": {" +
            "      \"filter\": {" +
            "        \"term\": {" +
            "          \"_id\": \"?0\"" +
            "        }" +
            "      }" +
            "    }" +
            "}";
    @Query(pattern)
    Journal findJournalById(String id);
}
