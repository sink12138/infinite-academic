package com.buaa.academic.document.entity.item;

import com.buaa.academic.document.entity.Patent;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "专利实体的简要信息，用于批量搜索的结果显示")
public class PatentItem {

    @Id
    @ApiModelProperty(value = "专利的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @ApiModelProperty(value = "专利标题", required = true, example = "笔记本电脑")
    private String title;

    @ApiModelProperty(value = "专利类型", example = "外观设计")
    private String type;

    @ApiModelProperty(value = "申请日", example = "2021-02-20")
    private String filingDate;

    @ApiModelProperty(value = "公开日", example = "2021-10-22")
    private String publicationDate;

    @ApiModelProperty(value = "发明人列表")
    private List<Patent.Inventor> inventors;

    @ApiModelProperty(value = "申请人", example = "宏碁股份有限公司")
    private String applicant;

}
