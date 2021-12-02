package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "热门学科简要信息")
public class SimpleSubject {
    @ApiModelProperty(value = "学科名称", example = "计算机科学")
    private String name;

    @ApiModelProperty(value = "热度", example = "0.114514")
    private Double heat;
}
