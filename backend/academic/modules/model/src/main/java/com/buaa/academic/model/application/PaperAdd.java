package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "新增论文具体内容")
public class PaperAdd implements Serializable {

    @Valid
    @NotNull
    @ApiModelProperty(value = "待新增的论文信息")
    private PaperForApp add;

}
