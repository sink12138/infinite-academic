package com.buaa.academic.document.entity.item;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "科研人员实体的简要信息，用于批量搜索的结果显示")
public class ResearcherHit {

    @ApiModelProperty(required = true, value = "科研人员的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @ApiModelProperty(required = true, value = "科研人员姓名", example = "谭火彬")
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("ResearcherHit$Institution")
    public static class Institution {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "所属机构的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
        @ApiModelProperty(required = true, value = "机构的名称", example = "北京航空航天大学软件学院")
        private String name;

    }

    @ApiModelProperty(value = "当前所属机构的信息")
    private Institution institution;

    @ApiModelProperty(value = "职称，例如讲师、副教授、教授等", example = "副教授")
    private String position;

    @ApiModelProperty(value = "科研人员的研究方向", example = "智能化软件工程")
    private List<String> interests;

    @ApiModelProperty(value = "科研人员发表的文章数量", example = "114514")
    private int paperNum = 0;

    @Field(type = FieldType.Integer)
    @ApiModelProperty(value = "科研人员发明的专利数量", example = "1919810")
    private int patentNum = 0;

}
