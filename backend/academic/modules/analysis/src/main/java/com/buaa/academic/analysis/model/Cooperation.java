package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "合作关系")
public class Cooperation {
    @ApiModelProperty(value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @ApiModelProperty(value = "实体名称", example = "谭火彬")
    private String name;

    @ApiModelProperty(value = "合作次数", example = "11")
    private Integer times;
}
