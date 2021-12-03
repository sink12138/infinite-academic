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
@Document(indexName = "topic")
@Setting(settingPath = "settings.json")
@ApiModel(description = "话题")
public class Topic {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
    @ApiModelProperty(value = "名称", example = "光刻技术")
    private String name;

    @Field(type = FieldType.Double)
    @ApiModelProperty(value = "热度", example = "11.4514")
    private double heat;

    @Field(type = FieldType.Object)
    @ApiModelProperty(value = "关联话题")
    private List<Association> associationTopics = new ArrayList<>();

    @ApiModelProperty(value = "该话题下每年发文量")
    @Field(type = FieldType.Object)
    private NumPerYear publicationData = new NumPerYear();
}
