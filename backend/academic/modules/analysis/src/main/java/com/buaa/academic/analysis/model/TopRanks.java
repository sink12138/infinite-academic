package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学科话题组合排名")
public class TopRanks<T> {

    private List<T> subjects;

    private List<T> topics;

}
