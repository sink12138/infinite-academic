package com.buaa.academic.search.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索结果模型")
public class HitPage<I> {

    @Value("${spring.elasticsearch.highlight.pre-tag")
    protected static String preTag;

    @Value("${spring.elasticsearch.highlight.post-tag")
    protected static String postTag;

    @ApiModelProperty(value = "搜索用时（毫秒）", required = true, example = "1919810")
    protected long timeCost;

    @ApiModelProperty(value = "总计搜索到的页数", required = true, example = "32")
    protected int totalPages;

    @ApiModelProperty(value = "总计搜索到的条目数量", required = true, example = "114514")
    protected long totalHits;

    @ApiModelProperty(value = "当前页码，从0开始", required = true, example = "2")
    protected int page;

    @ApiModelProperty(value = "当前页码上的项目数量", required = true, example = "10")
    protected int size;

    @ApiModelProperty(value = "当页的所有条目", required = true)
    protected List<I> items;

}
