package com.buaa.academic.document.system;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "application")
@Setting
public class Application {
    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请的数据库ID", example = "BcYuon0BGFAD_tUkFP7g")
    private String id;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请人用户id", example = "3cYPmX0BGFAD_tUkT_03")
    private String userId;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "联系邮箱", example = "114514@qq.com")
    private String email;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "申请时间", example = "2021-08-17 19:26")
    private String time;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请类型（学者认证/门户认领/门户信息修改/添加论文/下架论文/修改论文信息/专利转让）", example = "专利转让")
    private ApplicationType type;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请处理状态（审核中/审核通过/审核不通过）", example = "审核中")
    private StatusType status;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "申请附件对应的文件token", example = "B0KAR4A8Q3BHHHPMF0TT0I0B6YQMBAPPPHNPMIYM6GJMDZF4EQB7CLGOCP8S211R")
    private String fileToken;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "https://114514.com/paper?id=1919810")
    private String websiteLink;
}
