package com.buaa.academic.search.model;

import com.buaa.academic.search.validator.AllowValues;
import com.buaa.academic.search.validator.SearchCondition;
import com.buaa.academic.search.validator.SearchFilter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索请求体模型")
public class SearchRequest {

    @NotNull
    @Size(min = 1, max = 5)
    @ApiModelProperty(value = "所有搜索条件（顶层），不可为空", required = true)
    private List<@SearchCondition Condition> conditions;

    @NotNull
    @Size(max = 5)
    @ApiModelProperty(value = "数值过滤条件，可为空但不可为null")
    private List<@SearchFilter Filter> filter;

    @Range(min = 0)
    @ApiModelProperty(value = "查询的页码，从0开始", required = true, example = "2")
    private int page;

    @Range(min = 1, max = 25)
    @ApiModelProperty(value = "每页呈现的条目数量", required = true, example = "20", allowableValues = "range[1, 25]")
    private int size;

    @NotBlank
    @ApiModelProperty(value = "排序依据，null默认为相关度排序", example = "year")
    private String sort;

    @AllowValues({"red", "bold"})
    @ApiModelProperty(value = "搜索结果的高亮类型，null默认为无高亮", allowableValues = "red,bold")
    private String highlight;

}
