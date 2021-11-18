package com.buaa.academic.search.model.response;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.search.util.HighlightManager;
import com.buaa.academic.search.util.HitsReducer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "智能搜索结果模型")
public class SmartPage extends HitPage<PaperItem> {

    @ApiModelProperty(value = "检测到并推荐的实体类型，若为null表示没有推荐", allowableValues = "可能的值为researcher, institution, journal")
    protected String detection;

    @ApiModelProperty(value = "推荐实体列表，最多6个")
    protected List<?> recommendation;

    public void setHits(SearchHits<Paper> hits) {
        this.items = HitsReducer.reducePaperHits(hits, preTag, postTag);
    }

}
