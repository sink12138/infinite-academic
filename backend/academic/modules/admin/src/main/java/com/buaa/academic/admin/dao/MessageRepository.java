package com.buaa.academic.admin.dao;

import com.buaa.academic.document.system.Message;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends ElasticsearchRepository<Message, String> {

    void deleteByOwnerId(String ownerId);

}
