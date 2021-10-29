package com.buaa.academic.document.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "论文在数据库中的ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @ApiModelProperty(required = true, value = "论文标题", example = "基于机器学习的无需人工编制词典的切词系统")
    private String title;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的类别", allowableValues = "期刊论文, 学位论文, 图书", example = "期刊论文")
    private String type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("Paper$Author")
    public static class Author {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "论文作者的ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(required = true, value = "作者姓名", example = "谭火彬")
        private String name;

        @Field(type = FieldType.Integer)
        @ApiModelProperty(value = "作者所属机构的序号（从0开始），作为上标显示在作者名字之后")
        private List<Integer> instOrders;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的所有作者信息", required = true)
    private List<Author> authors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value = "Paper$Institution")
    public static class Institution {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "机构的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Text)
        @ApiModelProperty(required = true, value = "机构的名称", example = "北京航空航天大学软件学院")
        private String name;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的所有机构")
    private List<Institution> institutions;

    @Field(name = "abstract", type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @JsonProperty("abstract")
    @ApiModelProperty(required = true, value = "论文摘要", example = "假装这是一大段摘要")
    private String paperAbstract;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    @ApiModelProperty(required = true, value = "论文的所有关键词")
    private List<String> keywords;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的学科分类")
    private List<String> subjects;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的话题分类")
    private List<String> topics;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "论文的发表年份", example = "2021")
    private Integer year;

    @Field(type = FieldType.Integer, nullValue = "0")
    @ApiModelProperty(required = true, value = "论文被引量", example = "114")
    private Integer citationNum;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的DOI编号", example = "10.1007/BF02943174")
    private String doi;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的ISSN编号", example = "ISSN 1607-5161")
    private String issn;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的ISBN编号", example = "ISBN 978-7-302-12260-9")
    private String isbn;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("Paper$Journal")
    public static class Journal {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "期刊的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(required = true, value = "期刊标题", example = "Science")
        private String title;

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "论文在期刊中的卷号", example = "43")
        private String volume;

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "论文在期刊中的期号", example = "02")
        private String issue;

        @Field(type = FieldType.Integer)
        @ApiModelProperty(value = "论文在期刊中的起始页码", example = "114")
        private Integer startPage;

        @Field(type = FieldType.Integer)
        @ApiModelProperty(value = "论文在期刊中的终止页码", example = "514")
        private Integer endPage;

    }

    @Field(type = FieldType.Object)
    @ApiModelProperty(value = "论文的所属期刊信息")
    private Journal journal;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的出版商", example = "Elsevier")
    private String publisher;

    @Field(type = FieldType.Keyword, positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的所有参考文献的数据库ID")
    private List<String> references;

    @Field(type = FieldType.Keyword, positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的所有来源网页")
    private List<String> urls;

    @Field(type = FieldType.Keyword)
    @JsonIgnore
    @ApiModelProperty(value = "论文的源文件存储路径")
    private String filePath;

}
