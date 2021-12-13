package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学者认证具体内容")
public class Certification implements Serializable {

    @Valid
    @ApiModelProperty(value = "学者门户信息，若需新建门户则含有此字段")
    private PortalForApp create;

    @Valid
    @ApiModelProperty(value = "待认证的学者编号，若需认领已有门户则含有此字段")
    private Claim claim;

    @NotNull
    @Pattern(regexp = "^[0-9A-Za-z]{5}$")
    @ApiModelProperty(value = "验证码，只在提交POST时需要，查看GET时没有这个字段")
    private String code;

}
