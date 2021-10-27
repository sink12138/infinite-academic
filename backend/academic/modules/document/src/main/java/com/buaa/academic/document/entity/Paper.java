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
@Document(indexName = "paper")
public class Paper {

    @Id
    @Field(type = FieldType.Auto)
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    private String title;

    @Field(type = FieldType.Keyword)
    private String type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Author {

        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Integer)
        private List<Integer> instOrders;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    private List<Author> authors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Institution {

        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Text)
        private String name;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    private List<Institution> institutions;

    @Field(name = "abstract", type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @JsonProperty("abstract")
    private String paperAbstract;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    private List<String> keywords;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    private List<String> subjects;

    @Field(type = FieldType.Integer)
    private Integer year;

    @Field(type = FieldType.Integer)
    private Integer citationNum;

    @Field(type = FieldType.Keyword)
    private String doi;

    @Field(type = FieldType.Keyword)
    private String issn;

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Journal {

        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Keyword)
        private String title;

        @Field(type = FieldType.Keyword)
        private String volume;

        @Field(type = FieldType.Keyword)
        private String issue;

        @Field(type = FieldType.Integer)
        private Integer startPage;

        @Field(type = FieldType.Integer)
        private Integer endPage;

    }

    @Field(type = FieldType.Nested)
    private Journal journal;

    @Field(type = FieldType.Keyword)
    private String publisher;

    @Field(type = FieldType.Keyword, positionIncrementGap = 100)
    private List<String> references;

    @Field(type = FieldType.Keyword, positionIncrementGap = 100)
    private List<String> urls;

    @Field(type = FieldType.Keyword)
    private String filePath;

}
