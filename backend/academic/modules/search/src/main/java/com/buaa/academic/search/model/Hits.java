package com.buaa.academic.search.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索结果模型")
public class Hits<E> {

    @ApiModelProperty(value = "总计搜索到的条目数量", required = true, example = "114514")
    private long totalHits;

    @ApiModelProperty(value = "总计搜索到的页数", required = true, example = "32")
    private long totalPages;

    @ApiModelProperty(value = "搜索用时（毫秒）", required = true, example = "1919810")
    private long timeCost;

    @ApiModelProperty(value = "当前页码，从0开始", required = true, example = "2")
    private int page;

    @ApiModelProperty(value = "当前页码上的项目数量", required = true, example = "10")
    private int size;

    @ApiModelProperty(value = "当页的所有条目", required = true)
    private List<Hit<E>> hits;

}
