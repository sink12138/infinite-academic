package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "修改门户的具体信息")
public class Modification implements Serializable {

    @NotNull
    @ApiModelProperty(value = "修改后的门户信息，不修改的字段也要将原值传回")
    private PortalForApp info;

}
