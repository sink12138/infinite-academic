package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用于搜索条件过滤的搜索结果聚合")
public class SearchAggregation {

    @ApiModelProperty(value = "聚合字段名称（类别）", example = "学科")
    private String field;

    @ApiModelProperty(value = "所有聚合桶")
    private List<Frequency> buckets;

}
