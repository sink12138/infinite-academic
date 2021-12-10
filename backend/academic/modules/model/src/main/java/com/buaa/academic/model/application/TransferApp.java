package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "专利转让具体内容")
public class TransferApp implements Serializable {
    @ApiModelProperty(value = "专利数据库id")
    private String patentId;

    @ApiModelProperty(value = "专利标题")
    private String title;

    @ApiModelProperty(value = "转让方")
    private String transferor;

    @ApiModelProperty(value = "受让方")
    private String transferee;
}
