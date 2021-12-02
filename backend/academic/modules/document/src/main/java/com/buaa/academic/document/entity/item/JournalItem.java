package com.buaa.academic.document.entity.item;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "学术期刊实体的简要信息，用于批量搜索的结果显示")
public class JournalItem {

    @ApiModelProperty(value = "学术期刊的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @ApiModelProperty(value = "期刊标题", required = true, example = "计算机工程与应用")
    private String title;

    @ApiModelProperty(value = "主办单位", example = "华北计算技术研究所")
    private String sponsor;
}
