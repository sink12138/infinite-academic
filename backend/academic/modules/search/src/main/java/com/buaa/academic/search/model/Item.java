package com.buaa.academic.search.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索结果中的单个项目")
public class Item<I> {

    @ApiModelProperty(value = "条目对应的实体信息")
    private I item;

    @ApiModelProperty(value = "搜索匹配高亮信息。键为属性名，值是该属性的html高亮格式")
    private Map<String, String> highlights;

}
