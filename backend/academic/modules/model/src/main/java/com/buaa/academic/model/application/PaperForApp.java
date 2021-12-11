package com.buaa.academic.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文章具体内容，用于新增和修改论文")
public class PaperForApp implements Serializable {

    @NotNull
    @NotEmpty
    @Length(max = 128)
    @ApiModelProperty(value = "论文标题", required = true)
    private String title;

    @NotNull
    @NotEmpty
    @ApiModelProperty(value = "论文的类别", required = true, allowableValues = "期刊论文, 学位论文, 图书", example = "期刊论文")
    private String type;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperForApp$Author")
    public static class Author implements Serializable {

        @Pattern(regexp = "^[0-9A-Za-z_-]{20}$")
        @ApiModelProperty(value = "论文作者的ID，若含有此字段代表绑定已有作者，否则需要给出其他字段代表新增作者", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Length(max = 32)
        @ApiModelProperty(value = "作者姓名", example = "谭火彬")
        private String name;

        @Length(max = 64)
        @ApiModelProperty(value = "作者机构名", example = "北京航空航天大学软件学院")
        private String instName;

    }

    @ApiModelProperty(value = "论文的所有作者信息", required = true)
    private List<@NotNull Author> authors;

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

    @NotNull
    @NotEmpty
    @ApiModelProperty(value = "论文的所有机构", required = true)
    private List<@NotNull Institution> institutions;

    @NotNull
    @NotEmpty
    @JsonProperty("abstract")
    @ApiModelProperty(required = true, value = "论文摘要", example = "假装这是一大段摘要")
    private String paperAbstract;

    @NotNull
    @NotEmpty
    @ApiModelProperty(required = true, value = "论文的所有关键词")
    private List<@NotNull @NotEmpty String> keywords;

    @ApiModelProperty(value = "论文的学科分类")
    private List<@NotNull @NotEmpty String> subjects;

    @ApiModelProperty(value = "论文的话题分类")
    private List<@NotNull @NotEmpty String> topics;

    @Range(min = 1970, max = 2025)
    @ApiModelProperty(value = "论文的发表年份", example = "2021")
    private Integer year;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "论文的发表日期", example = "2021-10-15")
    private String date;

    @ApiModelProperty(value = "论文的DOI编号", example = "10.1007/BF02943174")
    private String doi;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperAddApp$Journal")
    public static class Journal implements Serializable {

        @Pattern(regexp = "^[0-9A-Za-z_-]{20}$")
        @ApiModelProperty(value = "期刊的数据库ID，若含有此字段代表绑定已有期刊，否则需指定title代表新增期刊", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Length(max = 128)
        @ApiModelProperty(required = true, value = "期刊标题", example = "Science")
        private String title;

        @ApiModelProperty(value = "论文在期刊中的卷号", example = "43")
        private String volume;

        @ApiModelProperty(value = "论文在期刊中的期号", example = "02")
        private String issue;

        @PositiveOrZero
        @ApiModelProperty(value = "论文在期刊中的起始页码", example = "114")
        private Integer startPage;

        @PositiveOrZero
        @ApiModelProperty(value = "论文在期刊中的终止页码", example = "514")
        private Integer endPage;

    }

    @ApiModelProperty(value = "论文的所属期刊信息")
    private Journal journal;

    @Length(max = 64)
    @ApiModelProperty(value = "论文的出版商", example = "Elsevier")
    private String publisher;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PaperForApp$Reference")
    public static class ReferencePaper implements Serializable {

        @Pattern(regexp = "^$[0-9A-Za-z_-]{20}")
        @ApiModelProperty(value = "引用论文的数据库ID，若含有此字段代表绑定已有论文，否则需指定论文标题")
        private String id;

        @Length(max = 128)
        @ApiModelProperty(value = "引用论文标题")
        private String title;

    }

    @ApiModelProperty(value = "引用的文章")
    private List<@NotNull ReferencePaper> referencePapers;

}
