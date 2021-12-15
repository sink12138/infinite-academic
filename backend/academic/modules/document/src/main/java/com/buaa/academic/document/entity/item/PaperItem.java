package com.buaa.academic.document.entity.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学术论文实体的简要信息，用于批量搜索的结果显示")
public class PaperItem {

    @ApiModelProperty(
            value = "用于显示数据库中不存在的参考文献，格式为该参考文献的引用格式（此时该参考文献只含有这一个字段）。此字段不会出现在除/references接口以外的其他任何场合。",
            example = "J. H. Saltzer,D. P. Reed,D. D. Clark.End-to-end arguments in system design[J].ACM transactions on computer systems,1984,2(4).277-288.")
    private String raw;

    @ApiModelProperty(required = true, value = "论文在数据库中的ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @ApiModelProperty(required = true, value = "论文标题", example = "基于机器学习的无需人工编制词典的切词系统")
    private String title;

    @ApiModelProperty(value = "论文的类别", allowableValues = "期刊论文, 学位论文, 图书", example = "期刊论文")
    private String type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperItem$Author")
    public static class Author {

        @ApiModelProperty(value = "论文作者的ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @ApiModelProperty(required = true, value = "作者姓名", example = "谭火彬")
        private String name;

    }

    @ApiModelProperty(value = "论文的所有作者信息", required = true)
    private List<Author> authors;

    @ApiModelProperty(required = true, value = "论文摘要，最多显示80个字", example = "假装这是一大段摘要")
    @JsonProperty("abstract")
    private String paperAbstract;

    @ApiModelProperty(required = true, value = "论文的所有关键词")
    private List<String> keywords;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperItem$Journal")
    public static class Journal {

        @ApiModelProperty(value = "期刊的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @ApiModelProperty(required = true, value = "期刊标题", example = "Science")
        private String title;

    }

    @ApiModelProperty(value = "论文的所属期刊信息")
    private Journal journal;

    @ApiModelProperty(value = "论文的发表日期", example = "2021-10-15")
    private String date;

    @ApiModelProperty(required = true, value = "论文被引量", example = "114")
    private int citationNum;

}
