package com.buaa.academic.document.entity;

import com.buaa.academic.document.entity.item.ResearcherHit;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "科研人员实体")
@Document(indexName = "researcher")
public class Researcher implements Reducible<ResearcherHit> {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "科研人员的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "科研人员姓名", example = "谭火彬")
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("Researcher$Institution")
    public static class Institution {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "所属机构的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
        @ApiModelProperty(required = true, value = "机构的名称", example = "北京航空航天大学软件学院")
        private String name;

    }

    @Field(type = FieldType.Object)
    @ApiModelProperty(value = "当前所属机构的信息")
    private Institution currentInst;

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    @ApiModelProperty(value = "曾经工作过的所有机构的信息")
    private List<Institution> institutions;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "职称，例如讲师、副教授、教授等", example = "副教授")
    private String position;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "科研人员的邮箱", example = "songyou@buaa.edu.cn")
    private String email;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员的H指数", example = "4")
    @JsonProperty("hIndex")
    private Integer hIndex;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员的G指数", example = "5")
    @JsonProperty("gIndex")
    private Integer gIndex;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100)
    @ApiModelProperty(value = "科研人员的研究方向", example = "智能化软件工程")
    private List<String> interests;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员发表的文章数量", example = "114514")
    private int paperNum = 0;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员发明的专利数量", example = "1919810")
    private int patentNum = 0;

    @Override
    public ResearcherHit reduce() {
        ResearcherHit hit = new ResearcherHit();
        hit.setId(id);
        hit.setName(name);
        hit.setInstitution(new ResearcherHit.Institution(currentInst.id, currentInst.name));
        hit.setPosition(position);
        hit.setInterests(interests);
        hit.setPaperNum(paperNum);
        hit.setPatentNum(patentNum);
        return hit;
    }
}
