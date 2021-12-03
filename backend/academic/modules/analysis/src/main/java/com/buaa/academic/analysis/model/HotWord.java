package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "热门词汇")
public class HotWord {

    @ApiModelProperty(value = "名称", example = "人工智能", notes = "可以为学科或者话题")
    private String name;

    @ApiModelProperty(value = "热度", example = "114514.114514")
    private Double heat;

}
