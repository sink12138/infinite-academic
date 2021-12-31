package com.buaa.academic.account.model;

import com.buaa.academic.document.system.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "一页的消息以及总页数")
public class MessagePage {

    @ApiModelProperty(value = "当前页的消息列表")
    List<Message> messages;

    @ApiModelProperty(value = "总页数")
    int totalPages;

}
