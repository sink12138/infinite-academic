package com.buaa.academic.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学者门户信息，用于学者认证与门户修改")
public class PortalForApp implements Serializable {

    @NotNull
    @NotBlank
    @Length(max = 32)
    @ApiModelProperty(value = "学者姓名")
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("PortalForApp$Institution")
    public static class Institution {

        @Pattern(regexp = "^[0-9A-Za-z_-]{20}$")
        @ApiModelProperty(value = "机构ID", notes = "数据库不存在的机构可以只传名称")
        private String id;

        @Length(max = 128)
        @ApiModelProperty(value = "机构名称")
        private String name;
    }

    @Valid
    @NotNull
    @ApiModelProperty(value = "当前机构")
    private Institution currentInst;

    @Valid
    @ApiModelProperty(value = "曾经所在机构")
    private List<@NotNull Institution> institutions;

    @PositiveOrZero
    @JsonProperty("hIndex")
    @ApiModelProperty(value = "h指数")
    private Integer hIndex;

    @PositiveOrZero
    @JsonProperty("gIndex")
    @ApiModelProperty(value = "g指数")
    private Integer gIndex;

    @Valid
    @NotNull
    @NotBlank
    @Size(max = 16)
    @ApiModelProperty(value = "研究兴趣")
    private List<String> interests;

}
