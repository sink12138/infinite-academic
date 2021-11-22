package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学科分析结果")
@Document(indexName = "subject")
public class Subject {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "关联学科")
    public static class AssociationSubject {
        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Double)
        private Double confidence;
    }

}
