package com.buaa.academic.account.model;

import com.buaa.academic.document.entity.item.PatentItem;
import com.buaa.academic.model.Application.TransferApplication;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferInfo {
    @ApiModelProperty(value = "专利转让申请信息", required = true)
    private TransferApplication transferApplication;

    @ApiModelProperty(value = "联系邮箱")
    private String contactEmail;

    @ApiModelProperty(value = "文件上传后返回的Token")
    private String fileToken;
}
