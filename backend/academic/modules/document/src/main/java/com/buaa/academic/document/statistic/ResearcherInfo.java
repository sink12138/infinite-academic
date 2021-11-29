package com.buaa.academic.document.statistic;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResearcherInfo {
    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "科研人员的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "科研人员姓名", example = "谭火彬")
    private String name;
}
