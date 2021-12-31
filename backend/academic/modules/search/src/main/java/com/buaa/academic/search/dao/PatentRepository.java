package com.buaa.academic.search.dao;

import com.buaa.academic.document.entity.Patent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatentRepository extends ElasticsearchRepository<Patent, String> {

    Patent findTopByPatentNum(String patentNum);

}
