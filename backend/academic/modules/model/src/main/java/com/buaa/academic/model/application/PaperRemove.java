package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "移除文章具体内容")
public class PaperRemove implements Serializable {

    @NotNull
    @Pattern(regexp = "^[0-9A-Za-z_-]{20}$")
    @ApiModelProperty(value = "待移除的文章ID")
    private String paperId;

    @NotNull
    @NotEmpty
    @Length(max = 300)
    @ApiModelProperty(value = "移除原因")
    private String reason;

}
