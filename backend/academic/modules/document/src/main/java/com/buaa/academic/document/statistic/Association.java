package com.buaa.academic.document.statistic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学科和话题的关联学科或话题")
public class Association {

    @ApiModelProperty(value = "名称", example = "人工智能")
    @Field(type = FieldType.Keyword)
    private String name;

    @ApiModelProperty(value = "关联关系置信度", example = "0.88")
    @Field(type = FieldType.Double)
    private double confidence;

}
