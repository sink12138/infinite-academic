package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "申请信息")
public class ApplicationInfo<T> {

    @NotNull
    @ApiModelProperty(value = "具体申请内容", required = true)
    private T content;

    @NotNull
    @Email
    @ApiModelProperty(value = "邮箱", notes = "申请人联系邮箱或学者个人邮箱")
    private String email;

    @Pattern(regexp = "^[0-9A-Z]{64}$")
    @ApiModelProperty(value = "相关文件上传后返回的token", notes = "证明材料或文章")
    private String fileToken;

    @URL
    @ApiModelProperty(value = "相关网站链接", notes = "证明网站或文章资源网站")
    private String websiteLink;

}
