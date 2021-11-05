package com.buaa.academic.search.model;

import com.buaa.academic.search.validator.AllowValues;
import com.buaa.academic.search.validator.SearchCondition;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "查询条件模型")
public class Condition {

    @AllowValues({"and", "or", "not"})
    @ApiModelProperty(value = "条件的逻辑类型", required = true, allowableValues = "and,or,not", example = "and")
    private String logic;

    @ApiModelProperty(value = "是否为复合条件。若为true则必须指定子条件，若为false则必须指定除子条件外所有属性", required = true)
    private boolean compound;

    @ApiModelProperty(value = "搜索关键词，长度最多为32个字符", required = true, example = "机器学习")
    private String keyword;

    @ApiModelProperty(value = "限定关键词的出现范围，均为属性名", required = true, example = "title")
    private Set<String> scope;

    @ApiModelProperty(value = "是否模糊搜索", required = true)
    private boolean fuzzy;

    @ApiModelProperty(value = "是否进行语种关联，若为true则需指定languages", required = true)
    private boolean translated;

    @AllowValues({"zh", "en"})
    @ApiModelProperty(value = "需要扩展的语言", allowableValues = "zh,en", example = "zh")
    private Set<String> languages;

    @SearchCondition
    @ApiModelProperty(value = "查询子条件")
    private List<Condition> subConditions;

}
