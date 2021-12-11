package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ApiModelProperty(value = "转让方")
    private String transferor;

    @ApiModelProperty(value = "受让方")
    private String transferee;

}
