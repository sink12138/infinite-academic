package com.buaa.academic.model.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "子任务模型")
public class Task {

    @ApiModelProperty(value = "子任务名称或代号", example = "CNKI-线程1")
    private String name;

    @ApiModelProperty(value = "子任务状态", example = "Writing results to database...")
    private String status;

}

