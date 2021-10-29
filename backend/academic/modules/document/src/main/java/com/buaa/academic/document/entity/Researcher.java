package com.buaa.academic.document.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "researcher")
public class Researcher {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Binary, index = false)
    private byte[] avatar;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Institution {

        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
        private String name;

    }

    @Field(type = FieldType.Object)
    private Institution currentInst;

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    private List<Institution> institutions;

    @Field(type = FieldType.Keyword)
    private String position;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Integer)
    @JsonProperty("hIndex")
    private Integer hIndex;

    @Field(type = FieldType.Integer)
    @JsonProperty("gIndex")
    private Integer gIndex;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    private List<String> interests;

    @Field(type = FieldType.Integer)
    private Integer paperNum;

    @Field(type = FieldType.Integer)
    private Integer patentNum;

}
