package com.buaa.academic.search.model.request;

import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "简要信息查询模型")
public class BriefRequest {

    @NotNull
    @AllowValues({"paper", "researcher", "journal", "institution", "patent"})
    @ApiModelProperty(value = "实体类型，五大实体，不多说了", example = "paper")
    private String entity;

    @Valid
    @NotNull
    @NotEmpty
    @Size(max = 30)
    @ApiModelProperty(value = "要查询的ID列表，不能为空，最多30个")
    private List<@NotNull @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String> ids;

}
