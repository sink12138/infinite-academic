package com.buaa.academic.search.service.impl;

import com.buaa.academic.search.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

@Service
public class InfoServiceImpl implements InfoService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public <T> T findDocument(Class<T> target, String id) {
        return template.get(id, target);
    }

    @Override
    public <T> boolean hasDocument(Class<T> target, String id) {
        return template.exists(id, target);
    }

}
