package com.buaa.academic.model.web;

import com.buaa.academic.model.exception.ExceptionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The common response model for all apis, including "success", "message" and "data".
 * @param <D> The real data type to be returned
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "响应体基本模型")
public class Result<D> {

    @ApiModelProperty(required = true, value = "操作是否成功", example = "true")
    private boolean success = true;

    @ApiModelProperty(value = "错误信息，若操作不成功则含有此字段", example = "参数格式非法")
    private String message;

    @ApiModelProperty(value = "若操作成功则可能附带此字段")
    private D data;

    public Result<D> withFailure(ExceptionType type) {
        this.success = false;
        this.message = type.getMessage();
        return this;
    }

    public Result<D> withFailure(String message) {
        this.success = false;
        this.message = message;
        return this;
    }

    public Result<D> withData(D data) {
        this.data = data;
        return this;
    }

}
