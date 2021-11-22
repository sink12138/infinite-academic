package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
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
        public String name;
        public String status;
    }

    List<JobStatus> jobStatuses = new ArrayList<>();

    public void addJObStatus(JobStatus jobStatus) {
        jobStatuses.add(jobStatus);
    }
}
