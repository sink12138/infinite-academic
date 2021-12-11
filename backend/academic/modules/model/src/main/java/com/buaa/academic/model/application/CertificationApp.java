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
@ApiModel(value = "学者认证具体内容")
public class CertificationApp implements Serializable {
    @ApiModelProperty(value = "学者姓名")
    private String name;

    @ApiModelProperty(value = "学者当前机构")
    private String currentInstName;

    @ApiModelProperty(value = "学者曾经所在机构")
    private List<String> institutionsName;

    @ApiModelProperty(value = "验证码")
    private String code;
}
