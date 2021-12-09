package com.buaa.academic.model.Application;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferApplication {
    @ApiModelProperty(value = "专利标题", required = true, example = "笔记本电脑")
    private String title;

    @ApiModelProperty(value = "专利类型", example = "外观设计")
    private String type;

    @ApiModelProperty(value = "申请（专利）号", example = "CN202130098898.1")
    private String patentNum;

    @ApiModelProperty(value = "代理机构", example = "北京同立钧成知识产权代理有限公司")
    private String agency;

    @ApiModelProperty(value = "代理人", example = "朱颖;刘芳")
    private String agent;

    @ApiModelProperty(value = "转让方", example = "北京同立钧成知识产权代理有限公司")
    private String transferor;

    @ApiModelProperty(value = "受让方", example = "北京航空航天大学")
    private String transferee;


}
