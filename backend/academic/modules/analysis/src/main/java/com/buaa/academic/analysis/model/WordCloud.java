package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "词云面板")
public class WordCloud {

    @ApiModelProperty(value = "学科")
    private List<Frequency> subjects;

    @ApiModelProperty(value = "话题")
    private List<Frequency> topics;

}
