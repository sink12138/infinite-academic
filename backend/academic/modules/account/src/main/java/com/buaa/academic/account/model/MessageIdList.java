package com.buaa.academic.account.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "消息id的列表")
public class MessageIdList {

    @NotNull
    @ApiModelProperty(value = "id列表")
    List<String> idList;

}
