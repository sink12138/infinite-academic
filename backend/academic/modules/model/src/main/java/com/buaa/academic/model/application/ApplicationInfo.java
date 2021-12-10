package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "申请信息")
public class ApplicationInfo<T> {
    @ApiModelProperty(value = "具体申请内容", required = true)
    private T application;

    @ApiModelProperty(value = "邮箱", notes = "申请人联系邮箱或学者个人邮箱")
    private String email;

    @ApiModelProperty(value = "相关文件上传后返回的Token", notes = "证明材料或文章")
    private String fileToken;

    @ApiModelProperty(value = "相关网站链接", notes = "证明网站或文章资源网站")
    private String websiteLink;
}
