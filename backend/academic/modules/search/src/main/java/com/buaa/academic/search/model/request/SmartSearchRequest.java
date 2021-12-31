package com.buaa.academic.search.model.request;

import com.buaa.academic.search.validator.SearchFilter;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "智能搜索请求体模型")
public class SmartSearchRequest {

    @NotNull
    @NotBlank
    @Length(max = 128)
    @ApiModelProperty(value = "查询词，超过32个字符将被截断，忽略头尾空白符", required = true, example = "机器学习")
    private String keyword;

    @ApiModelProperty(value = "是否进行语种关联，若为true则进行中英文联想", required = true)
    private boolean translated;

    @Valid
    @NotNull
    @Size(max = 8)
    @ApiModelProperty(value = "过滤条件，可为空不可为null", required = true)
    private List<@SearchFilter Filter> filters;

    @PositiveOrZero
    @ApiModelProperty(value = "查询页码，从0开始（页码大于0时，只会返回基准实体）", required = true, example = "0", allowableValues = "range[1,25]")
    private int page;

    @Range(min = 1, max = 50)
    @ApiModelProperty(value = "每页显示的条目数量，最多50", required = true, example = "20")
    private int size;

    @AllowValues({"date.asc", "date.desc", "citationNum.desc"})
    @ApiModelProperty(value = "排序依据，null默认为相关度排序，格式为${字段名}.${排序类型}", allowableValues = "date.asc, date.dsc, citationNum.desc", example = "date.desc")
    private String sort;

}
