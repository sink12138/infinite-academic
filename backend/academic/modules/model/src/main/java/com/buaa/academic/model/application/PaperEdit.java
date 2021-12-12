package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "修改论文具体内容")
public class PaperEdit {

    @NotNull
    @Pattern(regexp = "^[0-9A-Za-z_-]{20}$")
    @ApiModelProperty(value = "待修改的论文ID", example = "2cdGqX0BGFAD_tUkF0EN")
    private String paperId;

    @NotNull
    @ApiModelProperty(value = "待修改的论文信息，不修改的字段也要传回原值")
    private PaperForApp edit;

}