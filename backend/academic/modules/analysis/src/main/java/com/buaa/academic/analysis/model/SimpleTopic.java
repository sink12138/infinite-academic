package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "热门话题简要信息")
public class SimpleTopic {
    @ApiModelProperty(value = "名称", example = "人工智能")
    private String name;

    @ApiModelProperty(value = "热度", example = "114514.114514")
    private Double heat;
}
