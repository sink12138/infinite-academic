package com.buaa.academic.document.statistic;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionInfo {
    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "科研机构的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized", copyTo = "completion"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "phrase", type = FieldType.Text, analyzer = "jcseg_nlp", searchAnalyzer = "jcseg_nlp")})
    @ApiModelProperty(value = "科研机构的名称", required = true, example = "北京航空航天大学软件学院")
    private String name;
}
