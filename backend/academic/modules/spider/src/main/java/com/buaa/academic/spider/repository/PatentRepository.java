package com.buaa.academic.spider.repository;

import com.buaa.academic.document.entity.Patent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PatentRepository extends ElasticsearchRepository<Patent, String> {
    Patent findPatentByTitleAndPatentNum(String title, String patentNum);
}
