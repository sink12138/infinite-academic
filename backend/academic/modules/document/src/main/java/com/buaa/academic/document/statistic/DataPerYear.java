package com.buaa.academic.document.statistic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "对应年份文章发表量")
public class DataPerYear {
    @ApiModelProperty(value = "年份", example = "1979")
    @Field(type = FieldType.Integer)
    private Integer year;

    @ApiModelProperty(value = "文章发布数量", example = "114")
    @Field(type = FieldType.Integer)
    private Integer num;
}