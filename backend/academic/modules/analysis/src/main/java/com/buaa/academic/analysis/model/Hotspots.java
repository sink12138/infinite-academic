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
@ApiModel(description = "热点学科和话题")
public class Hotspots {

    @ApiModelProperty(value = "热门学科")
    private List<HotWord> subjects;

    @ApiModelProperty(value = "热点话题")
    private List<HotWord> topics;

}
