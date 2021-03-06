package com.buaa.academic.search.model.response;

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
public class HitPage<I> {

    @ApiModelProperty(value = "搜索用时（毫秒）", required = true, example = "1919810")
    protected long timeCost;

    @ApiModelProperty(value = "总计搜索到的页数", required = true, example = "32")
    protected int totalPages;

    @ApiModelProperty(value = "总计搜索到的条目数量", required = true, example = "114514")
    protected long totalHits;

    @ApiModelProperty(value = "当前页码，从0开始", required = true, example = "2")
    protected int page;

    @ApiModelProperty(value = "当页的所有条目", required = true)
    protected List<I> items;

    public void setStatistics(long totalHits, int pageNum, int pageSize) {
        this.totalHits = totalHits;
        this.totalPages = (int) ((totalHits + pageSize - 1) / pageSize);
        this.page = Math.max(0, Math.min(pageNum, totalPages - 1));
    }

}
