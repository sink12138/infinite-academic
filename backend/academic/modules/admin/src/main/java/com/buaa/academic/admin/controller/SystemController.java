package com.buaa.academic.admin.controller;

import com.buaa.academic.admin.client.AnalysisClient;
import com.buaa.academic.admin.model.ScheduleBoard;
import com.buaa.academic.admin.service.AuthValidator;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/system")
@Validated
@Api(tags = "系统控制相关")
public class SystemController {

    @Autowired
    private AuthValidator authValidator;

    @Autowired
    private AnalysisClient analysisClient;

    private final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @GetMapping("/schedules")
    @ApiOperation(value = "获取定时任务列表", notes = "获取当前已注册的所有定时任务的概览和详细信息")
    public Result<ScheduleBoard> schedules(@RequestHeader(name = "Auth") String auth) {
        Result<ScheduleBoard> result = new Result<>();
        if (!authValidator.headerCheck(auth)) {
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        List<FeignOperation<Schedule>> operations = new ArrayList<>() {
            {
                add(new FeignOperation<>("学科话题热点关联分析") {
                    @Override
                    public Result<Schedule> apply() {
                        return analysisClient.status(auth);
                    }
                });
            }
        };
        for (FeignOperation<Schedule> operation : operations) {
            operation.start();
        }
        int total = operations.size();
        int running = 0;
        List<Schedule> schedules = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (FeignOperation<Schedule> operation : operations) {
            try {
                operation.join(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (FeignOperation<Schedule> operation : operations) {
            Result<Schedule> operationResult = operation.getResult();
            if (operationResult == null) {
                logger.warn("Timeout when retrieving status of '" + operation.getTag() + '\'');
            }
            if (operationResult == null || !operationResult.isSuccess()) {
                failures.add(operation.getTag());
                continue;
            }
            Schedule schedule = operationResult.getData();
            if (schedule.isRunning())
                ++running;
            schedules.add(schedule);
        }
        ScheduleBoard board = new ScheduleBoard(total, running, schedules, failures);
        return result.withData(board);
    }

    @PostMapping("/start/{code}")
    @ApiOperation(value = "运行定时任务", notes = "立即运行某个定时任务")
    @ApiImplicitParam(
            name = "code",
            value = "定时任务代号，当前可用值：</br>" +
                    "<b>analysis-01</b> - 学科话题热点关联分析")
    public Result<Void> startSchedule(@RequestHeader(name = "Auth") String auth,
                                      @PathVariable("code") String code) {
        Result<Void> result = new Result<>();
        FeignOperation<Void> operation;
        switch (code) {
            case "ANALYSIS-ASSOCIATION" -> operation = new FeignOperation<>(code) {
                @Override
                public Result<Void> apply() {
                    return analysisClient.start(auth);
                }
            };
            case "UPDATE-CRAWLER" -> operation = new FeignOperation<>(code) {
                @Override
                public Result<Void> apply() {
                    return new Result<Void>().withFailure(ExceptionType.NOT_FOUND);
                }
            };
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        operation.start();
        try {
            operation.join(3000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Result<Void> operationResult = operation.getResult();
        if (operationResult == null) {
            logger.warn("Timeout when starting schedule '" + operation.getTag() + '\'');
            return result.withFailure(ExceptionType.FORWARD_TIMEOUT);
        }
        return operation.getResult();
    }

    @PostMapping("/stop/{code}")
    @ApiOperation(
            value = "停止定时任务",
            notes = "立即向某个定时任务发送停止信号。</br>" +
                    "该任务接到信号后可能不会立即停止；如果该任务当前正在进行关键动作（例如将结果写入数据库），其有权拒绝停止命令。")
    @ApiImplicitParam(
            name = "code",
            value = "定时任务代号，当前可用值：</br>" +
                    "<b>analysis-01</b> - 学科话题热点关联分析")
    public Result<Void> stopSchedule(@RequestHeader(name = "Auth") String auth,
                                     @PathVariable("code") String code) {
        Result<Void> result = new Result<>();
        FeignOperation<Void> operation;
        switch (code) {
            case "ANALYSIS-ASSOCIATION" -> operation = new FeignOperation<>(code) {
                @Override
                public Result<Void> apply() {
                    return analysisClient.stop(auth);
                }
            };
            case "UPDATE-CRAWLER" -> operation = new FeignOperation<>(code) {
                @Override
                public Result<Void> apply() {
                    return new Result<Void>().withFailure(ExceptionType.NOT_FOUND);
                }
            };
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        operation.start();
        try {
            operation.join(3000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Result<Void> operationResult = operation.getResult();
        if (operationResult == null) {
            logger.warn("Timeout when stopping schedule '" + operation.getTag() + '\'');
            return result.withFailure(ExceptionType.FORWARD_TIMEOUT);
        }
        return operation.getResult();
    }

}
