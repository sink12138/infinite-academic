package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "认领门户具体内容")
public class Claim implements Serializable {

    @NotNull
    @NotEmpty
    @ApiModelProperty(value = "要认领的学者门户ID，申请通过后会将所选门户合并到第一个门户")
    private List<@NotNull @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String> portals;

}
