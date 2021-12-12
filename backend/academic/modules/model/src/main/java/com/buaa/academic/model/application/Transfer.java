package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "专利转让具体内容")
public class Transfer implements Serializable {

    @NotNull
    @Pattern(regexp = "^[0-9A-Za-z_-]{20}$")
    @ApiModelProperty(value = "待操作的专利数据库ID")
    private String patentId;

    @NotNull
    @ApiModelProperty(value = "转让方")
    private String transferor;

    @NotNull
    @ApiModelProperty(value = "受让方")
    private String transferee;

    @Length(max = 64)
    @ApiModelProperty(value = "转让后的申请人，若不变更需传回原值", example = "宏碁股份有限公司")
    private String applicant;

    @Length(max = 128)
    @ApiModelProperty(value = "转让后的地址，若不变更需传回原值", example = "中国台湾新北市汐止区新台五路一段88号8楼")
    private String address;

    @Length(max = 64)
    @ApiModelProperty(value = "转让后的代理机构，若不变更需传回原值", example = "北京同立钧成知识产权代理有限公司")
    private String agency;

    @Length(max = 32)
    @ApiModelProperty(value = "转让后的代理人，若不变更需传回原值", example = "朱颖;刘芳")
    private String agent;

}
