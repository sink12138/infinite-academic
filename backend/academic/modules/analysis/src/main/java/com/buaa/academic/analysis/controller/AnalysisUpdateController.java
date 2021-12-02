package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Api(tags = "管理员用，更新数据分析结果", value = "/analysis/update")
@RequestMapping("/update")
@Validated
@RestController()
public class AnalysisUpdateController {

    private static String authHeader;

    @Autowired
    private AnalysisUpdateService analysisUpdateService;

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String password;

    @PostConstruct
    public void init() {
        if (authHeader == null)
            authHeader = Base64.getEncoder().encodeToString((username + '@' + password).getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidHeader(String auth) {
        return auth.equals(authHeader);
    }

    @ApiOperation(value = "开始数据分析", notes = "先后进行关联分析和热度分析，每个分析部分话题和学科会分两个线程同时执行")
    @PostMapping("/start")
    public Result<Void> startAnalysis(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();

        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        boolean notStarted = analysisUpdateService.start();

        if (notStarted)
            return result;
        else
            return result.withFailure("当前任务已在运行");
    }

    @ApiOperation(value = "查看当前分析作业执行情况")
    @GetMapping("/status")
    public Result<Schedule> checkAnalysisStatus(@RequestHeader(name = "Auth") String auth) {
        Result<Schedule> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        return result.withData(analysisUpdateService.getStatus());
    }

    @ApiOperation(value = "停止当前数据分析作业", notes = "为保证数据库中数据的一致性，数据分析在写入ES阶段不允许中途停止")
    @PostMapping("/stop")
    public Result<Void> stopAnalysis(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        analysisUpdateService.stop();
        return result;
    }

    @ApiOperation(value = "设置定时任务", notes = "最高频率为每天，最低频率为每月")
    @ApiImplicitParam(name = "cron", value = "定时任务cron表达式", example = "0 0 1 ? * L")
    @PostMapping("/timing")
    public Result<Void> autoUpdateSetting(@RequestHeader(name = "Auth") String auth,
                                          @RequestParam(value = "cron") @NotNull String cron) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        analysisUpdateService.autoStart(cron);
        return result;
    }
}
