package com.buaa.academic.search.model.request;

import com.buaa.academic.search.validator.SearchCondition;
import com.buaa.academic.search.validator.SearchFilter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索请求体模型")
public class SearchRequest {

    @Valid
    @NotNull
    @Size(min = 1, max = 12)
    @ApiModelProperty(value = "所有搜索条件（顶层），不可为空", required = true)
    private List<@NotNull @SearchCondition Condition> conditions;

    @Valid
    @NotNull
    @Size(max = 8)
    @ApiModelProperty(value = "过滤条件，可为空不可为null", required = true)
    private List<@NotNull @SearchFilter Filter> filters;

    @PositiveOrZero
    @ApiModelProperty(value = "查询的页码，从0开始", required = true, example = "2")
    private int page;

    @Range(min = 1, max = 50)
    @ApiModelProperty(value = "每页呈现的条目数量，最多50", required = true, example = "20", allowableValues = "range[1, 25]")
    private int size;

    @ApiModelProperty(value = "排序依据，null默认为相关度排序", example = "citationNum.desc")
    private String sort;

}
