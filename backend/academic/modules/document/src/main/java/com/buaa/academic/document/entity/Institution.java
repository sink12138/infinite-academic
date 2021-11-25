package com.buaa.academic.document.entity;

import com.buaa.academic.document.entity.item.InstitutionItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "科研机构实体")
@Document(indexName = "institution")
@Setting(settingPath = "settings.json")
public class Institution implements Reducible<InstitutionItem> {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "科研机构的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(type = FieldType.Keyword, suffix = "raw"))
    @ApiModelProperty(value = "科研机构的名称", required = true, example = "北京航空航天大学软件学院")
    private String name;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "学术机构的标志图片链接", example = "https://ma-v3-images.azureedge.net/images/185261750.png")
    private String logoUrl;

    @Override
    public InstitutionItem reduce() {
        InstitutionItem item = new InstitutionItem();
        item.setId(id);
        item.setName(name);
        return item;
    }
}
