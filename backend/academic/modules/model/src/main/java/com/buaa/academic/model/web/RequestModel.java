package com.buaa.academic.model.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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

    @ApiModelProperty(name = "num", required = true, value = "一个不大于5的正整数")
    @NotNull
    @Range(min = 0, max = 5)
    private Integer num;

    @ApiModelProperty(name = "code", required = true, value = "一个非空字符串")
    @NotNull
    @NotBlank
    private String code;

}
