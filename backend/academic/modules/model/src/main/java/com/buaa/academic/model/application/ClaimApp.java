package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "认领门户具体内容")
public class ClaimApp implements Serializable {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("ClaimApp$Portal")
    public static class Portal {
        @ApiModelProperty(value = "学者id")
        String id;
        @ApiModelProperty(value = "学者姓名")
        String name;
    }

    @ApiModelProperty(value = "要认领的学者门户")
    private List<Portal> portals;
}
