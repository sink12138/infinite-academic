package com.buaa.academic.account.repository;

import com.buaa.academic.document.entity.User;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface AccountRepository extends ElasticsearchRepository<User, String> {
    User findUserByEmail(String email);

    String pattern = "{\"constant_score\": {" +
            "      \"filter\": {" +
            "        \"term\": {" +
            "          \"_id\": \"?0\"" +
            "        }" +
            "      }" +
            "    }" +
            "}";
    @Query(pattern)
    User findUserById(String id);
}
