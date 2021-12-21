package com.buaa.academic.document.entity;

import com.buaa.academic.document.entity.item.PatentItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "专利实体")
@Document(indexName = "patent")
@Setting(settingPath = "settings.json")
public class Patent implements Reducible<PatentItem> {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "专利的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized", copyTo = "completion"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "phrase", type = FieldType.Text, analyzer = "thai_lowercase", searchAnalyzer = "thai_lowercase")})
    @ApiModelProperty(value = "专利标题", required = true, example = "笔记本电脑")
    private String title;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "专利类型", example = "外观设计")
    private String type;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请（专利）号", example = "CN202130098898.1")
    private String patentNum;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @ApiModelProperty(value = "申请日", example = "2021-02-20")
    private String fillingDate;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请公布号", example = "CN113519399A")
    private String publicationNum;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @ApiModelProperty(value = "公开公告日", example = "2021-10-22")
    private String publicationDate;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "授权公布号", example = "CN306893057S")
    private String authorizationNum;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @ApiModelProperty(value = "授权公告日", example = "2021-10-22")
    private String authorizationDate;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized", copyTo = "completion"),
            otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
    @ApiModelProperty(value = "申请人", example = "宏碁股份有限公司")
    private String applicant;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "地址", example = "中国台湾新北市汐止区新台五路一段88号8楼")
    private String address;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("Patent$Inventor")
    public static class Inventor {

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "发明人的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
        private String id;

        @Field(type = FieldType.Keyword)
        @ApiModelProperty(value = "发明人姓名", required = true, example = "曹容维")
        private String name;

    }

    @Field(type = FieldType.Object, positionIncrementGap = 100)
    @ApiModelProperty(value = "发明人信息列表")
    private List<Inventor> inventors;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "分类号", example = "14-02")
    private String classificationNum;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "主分类号", example = "14-02")
    private String mainClassificationNum;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "国省代码", example = "JP")
    private String countryProvinceCode;

    @Field(type = FieldType.Integer, index = false)
    @ApiModelProperty(value = "页数", example = "9")
    private Integer pageNum;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "代理机构", example = "北京同立钧成知识产权代理有限公司")
    private String agency;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "代理人", example = "朱颖;刘芳")
    private String agent;

    @JsonProperty("abstract")
    @MultiField(
            mainField = @Field(type = FieldType.Text, name = "abstract", analyzer = "ik_optimized", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
    @ApiModelProperty(value = "专利的摘要", example = "假装这是一大段摘要")
    private String patentAbstract;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "主权项", example = "假装这是一大段主权项")
    private String claim;

    @JsonIgnore
    @CompletionField(analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private Completion completion;

    @Override
    public PatentItem reduce() {
        PatentItem item = new PatentItem();
        item.setId(id);
        if (title != null)
            item.setTitle(title.length() > 128 ? title.substring(0, 128) + "..." : title);
        item.setType(type);
        item.setFilingDate(fillingDate);
        item.setPublicationDate(publicationDate);
        item.setInventors(inventors);
        item.setApplicant(applicant);
        return item;
    }

}
