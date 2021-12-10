package com.buaa.academic.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "新增文章具体内容")
public class PaperAddApp implements Serializable {
    private String title;

    @ApiModelProperty(value = "论文的类别", allowableValues = "期刊论文, 学位论文, 图书", example = "期刊论文")
    private String type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperAddApp$Author")
    public static class Author {

        @ApiModelProperty(value = "论文作者的ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @ApiModelProperty(required = true, value = "作者姓名", example = "谭火彬")
        private String name;

        @ApiModelProperty(value = "作者所属机构的序号（从0开始），作为上标显示在作者名字之后")
        private List<Integer> instOrders;

    }

    @ApiModelProperty(value = "论文的所有作者信息", required = true)
    private List<Author> authors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value = "PaperAddApp$Institution")
    public static class Institution {

        @ApiModelProperty(value = "机构的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @ApiModelProperty(required = true, value = "机构的名称", example = "北京航空航天大学软件学院")
        private String name;

    }

    @ApiModelProperty(value = "论文的所有机构")
    private List<Institution> institutions;

    @JsonProperty("abstract")
    @ApiModelProperty(required = true, value = "论文摘要", example = "假装这是一大段摘要")
    private String paperAbstract;

    @ApiModelProperty(required = true, value = "论文的所有关键词")
    private List<String> keywords;

    @ApiModelProperty(value = "论文的学科分类")
    private List<String> subjects;

    @ApiModelProperty(value = "论文的话题分类")
    private List<String> topics;

    @ApiModelProperty(value = "论文的发表年份", example = "2021")
    private Integer year;

    @ApiModelProperty(value = "论文的发表日期", example = "2021-10-15")
    private String date;

    @ApiModelProperty(value = "论文的DOI编号", example = "10.1007/BF02943174")
    private String doi;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperAddApp$Journal")
    public static class Journal {

        @ApiModelProperty(value = "期刊的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @ApiModelProperty(required = true, value = "期刊标题", example = "Science")
        private String title;

        @ApiModelProperty(value = "论文在期刊中的卷号", example = "43")
        private String volume;

        @ApiModelProperty(value = "论文在期刊中的期号", example = "02")
        private String issue;

        @ApiModelProperty(value = "论文在期刊中的起始页码", example = "114")
        private Integer startPage;

        @ApiModelProperty(value = "论文在期刊中的终止页码", example = "514")
        private Integer endPage;
    }

    @ApiModelProperty(value = "论文的所属期刊信息")
    private Journal journal;

    @ApiModelProperty(value = "论文的出版商", example = "Elsevier")
    private String publisher;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperAddApp$ReferencePaper")
    public static class ReferencePaper {
        @ApiModelProperty(value = "引用论文数据库id", notes = "数据库中存在的论文只需要id不需要以下信息，否则需要以下信息以保障引用功能")
        private String id;

        @ApiModelProperty(value = "引用论文标题")
        private String title;

        @ApiModelProperty(value = "引用论文类型")
        private String type;

        @ApiModelProperty(value = "引用论文的所有作者姓名")
        private List<String> authorsName;

        @ApiModelProperty(value = "引用论文的发表年份", example = "2021")
        private Integer year;

        @ApiModelProperty(value = "应用论文所属期刊标题")
        private String journalTitle;

        @ApiModelProperty(value = "引用论文在期刊中的卷号", example = "43")
        private String journalVolume;

        @ApiModelProperty(value = "引用论文在期刊中的期号", example = "02")
        private String journalIssue;

        @ApiModelProperty(value = "引用论文在期刊中的起始页码", example = "114")
        private Integer journalStartPage;

        @ApiModelProperty(value = "引用论文在期刊中的终止页码", example = "514")
        private Integer journalEndPage;
    }

    @ApiModelProperty(value = "引用的文章")
    private List<ReferencePaper> referencePapers;
}
