package com.buaa.academic.document.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "学术论文实体")
@Document(indexName = "paper")
public class Paper {

    @Id
    @Field(type = FieldType.Auto)
    @ApiModelProperty(required = true, value = "论文在数据库中的ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @ApiModelProperty(required = true, value = "论文标题", example = "基于机器学习的无需人工编制词典的切词系统")
    private String title;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的类别", allowableValues = "期刊论文, 学位论文")
    private String type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class Author {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "论文作者的ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(required = true, value = "作者姓名", example = "谭火彬")
        private String name;

        @Field(type = FieldType.Integer)
        @ApiModelProperty(value = "作者所属机构的序号（从0开始），作为上标显示在作者名字之后", allowableValues = "range[0, infinity]", example = "[0, 1]")
        private List<Integer> instOrders;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的所有作者信息", required = true)
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

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    private List<String> topics;

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
