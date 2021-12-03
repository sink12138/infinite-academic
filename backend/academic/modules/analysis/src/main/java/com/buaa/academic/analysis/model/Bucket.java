package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "频次统计")
public class Bucket {

    @ApiModelProperty(value = "实体或话题、学科名称", example = "人工智能")
    private String term;

    @ApiModelProperty(value = "频数", example = "114514",
            notes = "在学者关系网络该字段含义为合作次数，" +
            "在学者、机构、期刊词频统计部分该字段含义为在该话题或学科下发文数量，" +
            "在顶级期刊、学者、机构部分该字段含义为期刊、学者、机构的发文数量，" +
            "在搜索结果聚合部分该字段为在搜索结果中出现的次数")
    private int frequency;

}
