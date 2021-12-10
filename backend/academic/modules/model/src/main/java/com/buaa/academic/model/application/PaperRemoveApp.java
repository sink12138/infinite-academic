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
@ApiModel(value = "移除文章具体内容")
public class PaperRemoveApp implements Serializable {
    @ApiModelProperty(value = "文章Id")
    private String paperId;

    @ApiModelProperty(value = "文章标题")
    private String title;

    @ApiModelProperty(value = "移除原因")
    private String reason;
}
