package com.buaa.academic.document.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Association {

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Double)
    private double confidence;

}
