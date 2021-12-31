package com.buaa.academic.admin.model;

import com.buaa.academic.model.web.Schedule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "定时任务面板信息")
public class ScheduleBoard {

    @ApiModelProperty(value = "当前已注册的定时任务数量")
    private int total;

    @ApiModelProperty(value = "当前正在运行的定时任务数量")
    private int running;

    @ApiModelProperty(value = "所有成功获取到的定时任务详细信息")
    private List<Schedule> schedules = new ArrayList<>();

    @ApiModelProperty(value = "未能成功获取其状态的定时任务名称")
    private List<String> failures;

}
