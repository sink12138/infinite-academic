package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;

@Api(tags = "管理员用，更新数据分析结果", value = "/analysis/update")
@RequestMapping("/update")
@Validated
@RestController()
public class AnalysisUpdateController {

    @Autowired
    private AnalysisUpdateService analysisUpdateService;

    @ApiOperation(value = "开始数据分析", notes = "先后进行关联分析和热度分析，每个分析部分话题和学科会分两个线程同时执行")
    @PostMapping("/start")
    public Result<Object> startAnalysis() {
        boolean notStarted = analysisUpdateService.start();

        if(notStarted)
            return new Result<>();
        else
            return new Result<>().withFailure("Analysis program has been running...");
    }

    @ApiOperation(value = "查看当前分析作业执行情况")
    @GetMapping("/status")
    public Result<Schedule> checkAnalysisStatus() {
        return new Result<Schedule>().withData(analysisUpdateService.getStatus());
    }

    @ApiOperation(value = "停止当前数据分析作业", notes = "为保证数据库中数据的一致性，数据分析在写入ES阶段不允许中途停止")
    @PostMapping("/stop")
    public Result<Void> stopAnalysis() {
        analysisUpdateService.stop();
        return new Result<>();
    }

    @ApiOperation(value = "设置定时任务", notes = "最高频率为每天，最低频率为每月")
    @ApiImplicitParam(name = "cron", value = "定时任务cron表达式", example = "0 0 1 ? * L")
    @PostMapping("/auto")
    public Result<Void> autoUpdateSetting(@RequestParam(value = "cron") @NotNull String cron) {
        analysisUpdateService.autoStart(cron);
        return new Result<>();
    }
}
