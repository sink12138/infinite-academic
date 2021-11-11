package com.buaa.academic.search.model.response;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.search.util.HighlightManager;
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
        this.items = new ArrayList<>();
        HighlightManager manager = new HighlightManager(preTag, postTag);
        for (SearchHit<Paper> hit : hits) {
            PaperItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("title")) {
                String title = hlt.get("title").get(0);
                if (manager.text(title).length() > 32)
                    title = manager.cut(32).process() + "...";
                item.setTitle(title);
            }
            if (hlt.containsKey("keywords")) {
                item.setKeywords(hlt.get("keywords"));
            }
            if (hlt.containsKey("abstract")) {
                String paperAbstract = hlt.get("abstract").get(0);
                if (manager.text(paperAbstract).length() > 80)
                    paperAbstract = manager.cut(80).process() + "...";
                item.setPaperAbstract(paperAbstract);
            }
        }
    }

}
