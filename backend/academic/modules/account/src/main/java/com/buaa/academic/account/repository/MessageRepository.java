package com.buaa.academic.account.repository;

import com.buaa.academic.document.system.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageRepository extends ElasticsearchRepository<Message, String> {

    long countByOwnerIdAndRead(String ownerId, boolean read);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Message> findByOwnerIdOrderByTimeDesc(String owner, Pageable pageable);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    SearchPage<Message> findByOwnerIdAndReadOrderByTimeDesc(String ownerId, boolean read, Pageable pageable);

    void deleteByOwnerId(String ownerId);

}
