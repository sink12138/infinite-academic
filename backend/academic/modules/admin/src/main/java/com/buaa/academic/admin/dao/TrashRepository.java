package com.buaa.academic.admin.dao;

import com.buaa.academic.document.system.Trash;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrashRepository extends ElasticsearchRepository<Trash, String> {
}
