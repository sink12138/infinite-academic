package com.buaa.academic.account.utils;

import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.model.application.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExistenceCheck {
    @Autowired
    ElasticsearchRestTemplate template;

    public Boolean transferCheck(Transfer transfer) {
        return template.exists(transfer.getPatentId(), Patent.class);
    }
}
