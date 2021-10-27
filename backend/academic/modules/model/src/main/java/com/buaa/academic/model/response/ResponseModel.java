package com.buaa.academic.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(description = "响应体模型")
@Getter
public class ResponseModel<D> {

    @ApiModelProperty(name = "success", required = true, value = "操作是否成功")
    private boolean success = true;

    @ApiModelProperty(name = "message", value = "若操作不成功则含有此字段")
    private String message;

    @ApiModelProperty(name = "data", value = "若操作成功则可能附带此字段")
    private D data;

    public ResponseModel<D> withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public ResponseModel<D> withMessage(String message) {
        this.message = message;
        return this;
    }

    public ResponseModel<D> withData(D data) {
        this.data = data;
        return this;
    }

}
