package com.buaa.academic.search.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "建议词弹出框")
public class SuggestBox {

    @ApiModelProperty(value = "补全建议词", required = true)
    private List<String> completion = Collections.emptyList();

    @ApiModelProperty(value = "纠错建议词", required = true)
    private List<String> correction = Collections.emptyList();

}
