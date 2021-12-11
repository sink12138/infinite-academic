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
    private String id;

    @ApiModelProperty(value = "申请人用户id")
    @Field(type = FieldType.Keyword)
    private String userId;

    @ApiModelProperty(value = "联系邮箱")
    @Field(type = FieldType.Keyword)
    private String email;

    @ApiModelProperty(value = "申请时间")
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm")
    private String time;

    @ApiModelProperty(value = "申请类型（学者认证/门户认领/门户信息修改/添加论文/下架论文/修改论文信息/专利转让）", example = "专利转让")
    @Field(type = FieldType.Keyword)
    private ApplicationType type;

    @ApiModelProperty(value = "申请处理状态（审核中/审核通过/审核不通过）", example = "审核中")
    @Field(type = FieldType.Keyword)
    private StatusType status;

    @Field(type = FieldType.Keyword)
    private String fileToken;

    @Field(type = FieldType.Keyword)
    private String websiteLink;
}
