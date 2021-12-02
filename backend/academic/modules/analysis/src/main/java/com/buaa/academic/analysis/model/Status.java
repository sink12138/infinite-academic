package com.buaa.academic.analysis.model;

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
@ApiModel(description = "数据分析总执行情况")
public class Status {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(description = "当前执行的作业及其情况")
    public static class JobStatus {
        @ApiModelProperty(value = "作业姓名", example = "HOT_TOPIC_ANALYSIS")
        public String name;

        @ApiModelProperty(value = "执行状态信息", example = "All Down! Cost 2.3s. 2021年11月14日12:12")
        public String status;

        @ApiModelProperty(value = "是否在执行", example = "false")
        public Boolean isRunning;
    }

    @ApiModelProperty(value = "所有作业执行情况")
    List<JobStatus> jobStatuses = new ArrayList<>();

    public void addJObStatus(JobStatus jobStatus) {
        jobStatuses.add(jobStatus);
    }
}
