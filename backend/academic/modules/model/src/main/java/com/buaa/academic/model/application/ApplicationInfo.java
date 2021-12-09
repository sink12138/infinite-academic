package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationInfo<T> {
    @ApiModelProperty(value = "具体申请信息", required = true)
    private T application;

    @ApiModelProperty(value = "联系邮箱")
    private String contactEmail;

    @ApiModelProperty(value = "文件上传后返回的Token")
    private String fileToken;

    @ApiModelProperty(value = "证明网站链接")
    private String websiteLink;
}
