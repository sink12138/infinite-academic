package com.buaa.academic.spider.model.queueObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatentObject {
    private String url;

    private String patentId;
}
