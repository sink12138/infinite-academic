package com.buaa.academic.admin.controller;

import com.buaa.academic.admin.client.AnalysisClient;
import com.buaa.academic.admin.client.FeignOperation;
import com.buaa.academic.admin.model.ScheduleBoard;
import com.buaa.academic.admin.service.AuthValidator;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.tool.validator.CronExpr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @PostMapping("/start")
    @ApiOperation(value = "运行定时任务", notes = "立即运行某个定时任务")
    @ApiImplicitParam(
            name = "code",
            value = "定时任务代号，当前可用值：</br>" +
                    "<b>ANALYSIS-ASSOCIATION</b> - 学科话题热点关联分析")
    public Result<Void> startSchedule(@RequestHeader(name = "Auth") String auth,
                                      @RequestParam(name = "code") @NotNull @NotEmpty String code) {
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

    @PostMapping("/stop")
    @ApiOperation(
            value = "停止定时任务",
            notes = "立即向某个定时任务发送停止信号。</br>" +
                    "该任务接到信号后可能不会立即停止；如果该任务当前正在进行关键动作（例如将结果写入数据库），其有权拒绝停止命令。")
    @ApiImplicitParam(
            name = "code",
            value = "定时任务代号，当前可用值：</br>" +
                    "<b>ANALYSIS-ASSOCIATION</b> - 学科话题热点关联分析")
    public Result<Void> stopSchedule(@RequestHeader(name = "Auth") String auth,
                                     @RequestParam(name = "code") String code) {
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

    @PostMapping("/timing")
    @ApiOperation(
            value = "设置定时任务的执行频率",
            notes = "通过cron表达式设置某个定时任务的执行频率。</br>" +
                    "此处并不支持完全体的cron表达式，其中的通配符只允许使用*和?，表示星期几的最后一位只允许使用数字而非缩写。</br>" +
                    "同时，设定的执行频率不得高于每日一次（即前两位必须为0，第三位必须为数字。</br>" +
                    "<a href='https://blog.csdn.net/sunnyzyq/article/details/98597252'>什么是cron表达式？</a>")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "code",
                    value = "定时任务代号，当前可用值：</br>" +
                            "<b>ANALYSIS-ASSOCIATION</b> - 学科话题热点关联分析"),
            @ApiImplicitParam(
                    name = "cron",
                    value = "一串6位的cron表达式")})
    public Result<Void> timingSchedule(@RequestHeader(name = "Auth") String auth,
                                       @RequestParam("code") String code,
                                       @RequestParam("cron") @CronExpr String cron) {
        Result<Void> result = new Result<>();
        FeignOperation<Void> operation;
        switch (code) {
            case "ANALYSIS-ASSOCIATION" -> operation = new FeignOperation<>(code) {
                @Override
                public Result<Void> apply() {
                    return analysisClient.timing(auth, cron);
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
            logger.warn("Timeout when setting timing of schedule '" + operation.getTag() + '\'');
            return result.withFailure(ExceptionType.FORWARD_TIMEOUT);
        }
        return operation.getResult();
    }

}
