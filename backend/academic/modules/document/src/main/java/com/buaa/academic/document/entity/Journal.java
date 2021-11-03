package com.buaa.academic.document.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学术期刊实体")
@Document(indexName = "journal")
public class Journal {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "学术期刊的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "学术期刊自带的编号", example = "137773608")
    private String journalId;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @ApiModelProperty(value = "期刊标题", required = true, example = "计算机工程与应用")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @ApiModelProperty(value = "主办单位", example = "华北计算技术研究所")
    private String sponsor;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    @ApiModelProperty(value = "期刊的ISSN编号", example = "1002-8331")
    private String issn;

}
