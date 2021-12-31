package com.buaa.academic.admin.model;

import com.buaa.academic.document.system.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "申请详细信息")
public class ApplicationDetails<C> {

    @ApiModelProperty(value = "申请的基本概览信息")
    private Application basic;

    @ApiModelProperty(value = "申请的详细内容，不同申请类型内容结构有所不同")
    private C content;

}
