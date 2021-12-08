package com.buaa.academic.document.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "消息提醒实体")
@Document(indexName = "message")
@Setting
public class Message {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "消息的数据库ID", example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @JsonIgnore
    @Field(type = FieldType.Keyword)
    private String ownerId;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "消息标题", example = "学者认证成功")
    private String title;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "消息正文", example = "尊敬的用户 野兽仙贝，您的学者身份认证成功。")
    private String content;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    private String time;

    @Field(type = FieldType.Boolean)
    @ApiModelProperty(value = "是否已读", example = "false")
    private boolean read;

}
