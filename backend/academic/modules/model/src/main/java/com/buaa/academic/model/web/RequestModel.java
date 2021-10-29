package com.buaa.academic.model.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @param <D> The data type needed in the request body
 * @deprecated This class is only for test uses and will be removed soon in formal development.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "请求体模型（仅供测试用）")
public class RequestModel<D> {

    @ApiModelProperty(name = "data", required = true, value = "所需参数")
    @NotNull
    private D data;

}