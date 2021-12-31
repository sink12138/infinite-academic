package com.buaa.academic.scholar.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "自动爬取请求")
public class AutoPaper {

    @ApiModelProperty(value = "文章标题，若需要按URL爬取则为null")
    private String title;

    @URL
    @ApiModelProperty(value = "文章URL链接，若需要按标题爬取则为null")
    private String url;

}
