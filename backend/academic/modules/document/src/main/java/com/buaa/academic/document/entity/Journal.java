package com.buaa.academic.document.entity;

import com.buaa.academic.document.entity.item.JournalItem;
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
@ApiModel(description = "学术期刊实体")
@Document(indexName = "journal")
public class Journal implements Reducible<JournalItem> {

    @Id
    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "学术期刊的数据库ID", required = true, example = "GF_4ynwBF-Mu8unTG1hc")
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(type = FieldType.Keyword, suffix = "raw"))
    @ApiModelProperty(value = "期刊标题", required = true, example = "计算机工程与应用")
    private String title;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "期刊封面链接", example = "https://ss0.bdstatic.com/9r-1bjml2gcT8tyhnq/ps-scholar/xueshu/1628135451/25407/7dcab4875c10fd0e47ded29c6d9a703d.jpg")
    private String coverUrl;

    @Field(type = FieldType.Keyword, index = false)
    @ApiModelProperty(value = "主办单位", example = "华北计算技术研究所")
    private String sponsor;

    @Field(type = FieldType.Keyword)
    @ApiModelProperty(value = "期刊的ISSN编号", example = "1002-8331")
    private String issn;

    @Override
    public JournalItem reduce() {
        JournalItem item = new JournalItem();
        item.setId(id);
        item.setTitle(title);
        item.setSponsor(sponsor);
        return item;
    }

}
