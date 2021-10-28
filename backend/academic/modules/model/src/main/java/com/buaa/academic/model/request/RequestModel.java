package com.buaa.academic.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestModel<D> {

    @ApiModelProperty(name = "data", required = true, value = "所需参数")
    private D data;

}
