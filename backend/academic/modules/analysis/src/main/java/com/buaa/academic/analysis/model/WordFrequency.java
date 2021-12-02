package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "词频")
public class WordFrequency {
    @ApiModelProperty(value = "词语", example = "人工智能")
    private String word;

    @ApiModelProperty(value = "频数", example = "114514")
    private int frequency;
}
