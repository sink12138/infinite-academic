package com.buaa.academic.document.entity;

import com.buaa.academic.document.entity.item.ResearcherItem;
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
public class Researcher implements Reducible<ResearcherItem> {

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

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "科研人员的头像图片链接", example = "https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=3607928980,2147845641&fm=58")
    private String avatarUrl;

    @Field(type = FieldType.Object)
    @ApiModelProperty(value = "当前所属机构的信息")
    private Institution currentInst;

    @Field(type = FieldType.Nested, positionIncrementGap = 100)
    @ApiModelProperty(value = "曾经工作过的所有机构的信息")
    private List<Institution> institutions;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员的H指数", example = "4")
    @JsonProperty("hIndex")
    private Integer hIndex;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员的G指数", example = "5")
    @JsonProperty("gIndex")
    private Integer gIndex;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized", positionIncrementGap = 100, fielddata = true)
    @ApiModelProperty(value = "科研人员的研究方向", example = "智能化软件工程")
    private List<String> interests;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员发表的文章数量", example = "114514")
    private int paperNum = 0;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员发明的专利数量", example = "1919810")
    private int patentNum = 0;

    @Override
    public ResearcherItem reduce() {
        ResearcherItem item = new ResearcherItem();
        item.setId(id);
        item.setName(name);
        item.setInstitution(new ResearcherItem.Institution(currentInst.id, currentInst.name));
        item.setInterests(interests);
        item.setPaperNum(paperNum);
        item.setPatentNum(patentNum);
        return item;
    }
}
