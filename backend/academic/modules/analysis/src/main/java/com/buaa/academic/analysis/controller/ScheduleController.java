package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.service.AnalysisUpdateService;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.tool.validator.CronExpr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApiIgnore
@RequestMapping("/schedule")
@Validated
@RestController
public class ScheduleController {

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

    @PostMapping("/start")
    public Result<Void> startAnalysis(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();

        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        boolean started = analysisUpdateService.start();

        if (started)
            return result;
        else
            return result.withFailure("当前任务已在运行");
    }

    @GetMapping("/status")
    public Result<Schedule> checkAnalysisStatus(@RequestHeader(name = "Auth") String auth) {
        Result<Schedule> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        return result.withData(analysisUpdateService.getStatus());
    }

    @PostMapping("/stop")
    public Result<Void> stopAnalysis(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        analysisUpdateService.stop();
        return result;
    }

    @PostMapping("/timing")
    public Result<Void> autoUpdateSetting(@RequestHeader(name = "Auth") String auth,
                                          @RequestParam(value = "cron") @CronExpr String cron) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        analysisUpdateService.autoStart(cron);
        return result;
    }
}
