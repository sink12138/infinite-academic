package com.buaa.academic.search.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "滚动型的页面。通常情况下，每一页（每一次滚动）能获取的条目数量最多为10。")
public class ScrollPage<I> {

    @ApiModelProperty(value = "是否还能滚动（有下一页）", required = true, example = "true")
    private boolean hasMore;

    @ApiModelProperty(value = "当前页的所有条目，可能为空，但不会为null", required = true)
    private List<I> items;

}
