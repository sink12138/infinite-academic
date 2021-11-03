package com.buaa.academic.document.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@ApiModel(description = "普通用户账号实体")
@Document(indexName = "user")
public class User {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "用户在数据库中对应的ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "用户登录用的邮箱", example = "19231120@buaa.edu.cn")
    private String email;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "用户名", example = "yq")
    private String username;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(required = true, value = "用户设置的密码", example = "114514")
    private String password;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "用户认证的研究人员ID（如果已认证）", example = "GF_4ynwBF-Mu8unTG1hc")
    private String researcherId;

    @Field(type = FieldType.Keyword, nullValue = "false")
    @JsonIgnore
    private boolean verified;

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
