package com.buaa.academic.search.model;

import com.buaa.academic.document.entity.Reducible;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索结果模型")
public class HitPage<I, E extends Reducible<I>> {

    @ApiModelProperty(value = "搜索用时（毫秒）", required = true, example = "1919810")
    private long timeCost;

    @ApiModelProperty(value = "总计搜索到的页数", required = true, example = "32")
    private int totalPages;

    @ApiModelProperty(value = "总计搜索到的条目数量", required = true, example = "114514")
    private long totalHits;

    @ApiModelProperty(value = "当前页码，从0开始", required = true, example = "2")
    private int page;

    @ApiModelProperty(value = "当前页码上的项目数量", required = true, example = "10")
    private int size;

    @ApiModelProperty(value = "当页的所有条目", required = true)
    private List<Item<I>> items;

    public HitPage(long timeCost, SearchPage<E> searchPage) {
        SearchHits<E> searchHits = searchPage.getSearchHits();
        this.timeCost = timeCost;
        this.totalPages = searchPage.getTotalPages();
        this.totalHits = searchPage.getSearchHits().getTotalHits();
        this.page = searchPage.getNumber();
        this.size = searchPage.getSize();
        this.items = new ArrayList<>();
        for (SearchHit<E> searchHit : searchHits) {
            Map<String, String> highlights = new HashMap<>();
            Map<String, List<String>> fields = searchHit.getHighlightFields();
            for (String field : fields.keySet())
                highlights.put(field, fields.get(field).get(0));
            this.items.add(new Item<>(searchHit.getContent().reduce(), highlights));
        }
    }

}
