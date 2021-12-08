package com.buaa.academic.account.repository;

import com.buaa.academic.document.system.Message;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageRepository extends ElasticsearchRepository<Message, String> {
}
