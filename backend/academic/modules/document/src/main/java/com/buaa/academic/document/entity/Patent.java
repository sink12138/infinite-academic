package com.buaa.academic.document.entity;

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
@Document(indexName = "patent")
public class Patent {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    private String title;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String patentNum;

    @Field(type = FieldType.Keyword)
    private String filingDate;

    @Field(type = FieldType.Keyword)
    private String publicationNum;

    @Field(type = FieldType.Keyword)
    private String publicationDate;

    @Field(type = FieldType.Keyword)
    private String authorizationNum;

    @Field(type = FieldType.Keyword)
    private String authorizationDate;

    @Field(type = FieldType.Keyword)
    private String applicant;

    @Field(type = FieldType.Keyword)
    private String address;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Inventor {

        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Keyword)
        private String name;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    private List<Inventor> inventors;

    @Field(type = FieldType.Keyword)
    private String classificationNum;

    @Field(type = FieldType.Keyword)
    private String mainClassificationNum;

    @Field(type = FieldType.Keyword)
    private String countryProvinceCode;

    @Field(type = FieldType.Keyword)
    private String agency;

    @Field(type = FieldType.Keyword)
    private String agent;

}
