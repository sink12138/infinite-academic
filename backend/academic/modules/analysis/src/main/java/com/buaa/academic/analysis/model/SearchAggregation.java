package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用于搜索过滤的搜索结果聚合")
public class SearchAggregation {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "聚合结果中的一项")
    public static class item {
        @ApiModelProperty(value = "名称", example = "计算机科学")
        private String name;

        @ApiModelProperty(value = "结果中包含该项的文章数量", example = "5")
        private Integer num;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "对某一类别的聚合")
    public static class aggregationTerm {
        @ApiModelProperty(value = "聚合类别", example = "学科")
        private String term;

        @ApiModelProperty(value = "在该类别下的所有聚合结果")
        private ArrayList<item> items;
    }

    @ApiModelProperty(value = "所有聚合结果")
    private ArrayList<aggregationTerm> aggregationTerms;
}
