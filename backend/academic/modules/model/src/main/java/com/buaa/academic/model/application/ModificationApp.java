package com.buaa.academic.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "修改学者信息具体内容", description = "没有修改的字段要将原先的值传回")
public class ModificationApp implements Serializable {
    @ApiModelProperty(value = "修改后学者姓名")
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel("ModificationApp$Institution")
    public static class Institution {
        @ApiModelProperty(value = "机构Id", notes = "数据库不存在的机构可以只传名称")
        private String id;

        @ApiModelProperty(value = "机构名称")
        private String name;
    }

    @ApiModelProperty(value = "当前机构")
    private Institution currentInst;

    @ApiModelProperty(value = "曾经所在机构")
    private List<Institution> institutions;

    @ApiModelProperty(value = "h指数")
    private String hIndex;

    @ApiModelProperty(value = "g指数")
    private String gIndex;

    @ApiModelProperty(value = "研究兴趣")
    private List<String> interests;
}
