package com.buaa.academic.search.model.response;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.item.PaperItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "智能搜索结果模型")
public class SmartPage extends HitPage<PaperItem> {

    @ApiModelProperty(
            value = "检测到可能的搜索实体类型",
            notes = "同时也指定了推荐列表的实体类型。</br>" +
                    "可能的值为researcher, institution, journal；若为null则代表没有推荐内容。")
    protected String detection;

    @ApiModelProperty(
            value = "推荐实体列表，最多6个",
            notes = "这个字段的结构取决于具体推荐的实体类型，可能为科研人员、科研机构或学术期刊")
    protected List<?> recommendation;

}
