package com.buaa.academic.account.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "消息统计数据")
public class MessageStatistic {
    @ApiModelProperty(value = "未读消息总数")
    Integer unreadCount;
}
