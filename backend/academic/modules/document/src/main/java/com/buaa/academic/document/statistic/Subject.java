package com.buaa.academic.document.statistic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "subject")
@Setting(settingPath = "settings.json")
@ApiModel(description = "学科")
public class Subject {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
    @ApiModelProperty(value = "名称", example = "计算机")
    private String name;

    @ApiModelProperty(value = "热度", example = "114.514")
    @Field(type = FieldType.Double)
    private double heat;

    @ApiModelProperty(value = "关联学科")
    @Field(type = FieldType.Object)
    private List<Association> associationSubjects = new ArrayList<>();

    @ApiModelProperty(value = "该学科下每年发文数量")
    @Field(type = FieldType.Object)
    private DataByYear publicationData = new DataByYear();
}
