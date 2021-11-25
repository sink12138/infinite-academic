package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "话题分析结果")
@Document(indexName = "topic")
public class Topic {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Double)
    private Double hot;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "关联话题")
    public static class AssociationTopic {
        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Double)
        private Double confidence;
    }

    private ArrayList<AssociationTopic> associationTopics = new ArrayList<>();
}
