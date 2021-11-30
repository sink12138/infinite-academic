package com.buaa.academic.document.entity;

import com.buaa.academic.document.entity.item.PaperItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学术论文实体")
@Document(indexName = "paper")
@Setting(settingPath = "settings.json")
public class Paper implements Reducible<PaperItem> {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "论文在数据库中的ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized", copyTo = "completion"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "phrase", type = FieldType.Text, analyzer = "thai_lowercase", searchAnalyzer = "thai_lowercase")})
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

        @Field(type = FieldType.Integer, index = false)
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

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized"),
                otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
        @ApiModelProperty(required = true, value = "机构的名称", example = "北京航空航天大学软件学院")
        private String name;

    }

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    @ApiModelProperty(value = "论文的所有机构")
    private List<Institution> institutions;

    @JsonProperty("abstract")
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "abstract", analyzer = "ik_optimized", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
    @ApiModelProperty(required = true, value = "论文摘要", example = "假装这是一大段摘要")
    private String paperAbstract;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized",
                    positionIncrementGap = 100, copyTo = "completion"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "phrase", type = FieldType.Text, analyzer = "thai_lowercase", searchAnalyzer = "thai_lowercase")})
    @ApiModelProperty(required = true, value = "论文的所有关键词")
    private List<String> keywords;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized",
                    positionIncrementGap = 100, copyTo = "completion"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "phrase", type = FieldType.Text, analyzer = "thai_lowercase", searchAnalyzer = "thai_lowercase")})
    @ApiModelProperty(value = "论文的学科分类")
    private List<String> subjects;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized",
                    positionIncrementGap = 100, copyTo = "completion"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "phrase", type = FieldType.Text, analyzer = "thai_lowercase", searchAnalyzer = "thai_lowercase")})
    @ApiModelProperty(value = "论文的话题分类")
    private List<String> topics;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "论文的发表年份", example = "2021")
    private Integer year;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @ApiModelProperty(value = "论文的发表日期", example = "2021-10-15")
    private String date;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(required = true, value = "论文被引量", example = "114")
    private int citationNum;

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

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized"),
                otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
        @ApiModelProperty(required = true, value = "期刊标题", example = "Science")
        private String title;

        @Field(type = FieldType.Keyword, index = false)
        @ApiModelProperty(value = "论文在期刊中的卷号", example = "43")
        private String volume;

        @Field(type = FieldType.Keyword, index = false)
        @ApiModelProperty(value = "论文在期刊中的期号", example = "02")
        private String issue;

        @Field(type = FieldType.Integer, index = false)
        @ApiModelProperty(value = "论文在期刊中的起始页码", example = "114")
        private Integer startPage;

        @Field(type = FieldType.Integer, index = false)
        @ApiModelProperty(value = "论文在期刊中的终止页码", example = "514")
        private Integer endPage;

    }

    @Field(type = FieldType.Object)
    @ApiModelProperty(value = "论文的所属期刊信息")
    private Journal journal;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "论文的出版商", example = "Elsevier")
    private String publisher;

    @JsonIgnore
    @Field(type = FieldType.Keyword)
    private List<String> references;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("Paper$Source")
    public static class Source {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "来源网站名称", example = "ResearchGate")
        private String website;

        @Field(type = FieldType.Keyword, index = false)
        @ApiModelProperty(value = "来源网址", example = "http://www.researchgate.net/publication/273232093_The_Characteristics_of_Organizational_Environments_and_Perceived_Environmental_Uncertainty")
        private String url;

    }

    @ApiModelProperty(value = "论文的所有来源")
    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    private List<Source> sources;

    @JsonIgnore
    @Field(type = FieldType.Keyword, index = false)
    private String filePath;

    @JsonIgnore
    @CompletionField(analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private Completion completion;

    @Override
    public PaperItem reduce() {
        PaperItem item = new PaperItem();
        item.setId(id);
        if (title != null)
            item.setTitle(title.length() > 64 ? title.substring(0, 64) + "..." : title);
        item.setType(type);
        if (authors != null) {
            List<PaperItem.Author> hitAuthors = new ArrayList<>();
            authors.forEach(author -> hitAuthors.add(new PaperItem.Author(author.id, author.name)));
            item.setAuthors(hitAuthors);
        }
        if (paperAbstract != null)
            item.setPaperAbstract(paperAbstract.length() > 128 ? paperAbstract.substring(0, 128) + "..." : paperAbstract);
        item.setKeywords(keywords);
        if (journal != null)
            item.setJournal(new PaperItem.Journal(journal.id, journal.title));
        item.setDate(date == null ? year == null ? null : String.valueOf(year) : date);
        item.setCitationNum(citationNum);
        return item;
    }

}
