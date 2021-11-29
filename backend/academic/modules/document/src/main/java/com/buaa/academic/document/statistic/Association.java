package com.buaa.academic.document.statistic;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class Association {

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Double)
    private double confidence;

}
