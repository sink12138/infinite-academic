package com.buaa.academic.document.entity.item;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "科研机构实体的简要信息，用于批量搜索的结果显示")
public class InstitutionItem {

    @ApiModelProperty(value = "科研机构的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @ApiModelProperty(value = "科研机构的名称", required = true, example = "北京航空航天大学软件学院")
    private String name;


}
