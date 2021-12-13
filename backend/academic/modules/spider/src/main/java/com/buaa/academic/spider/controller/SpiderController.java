package com.buaa.academic.spider.controller;

import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.spider.util.StatusCtrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class SpiderController {
    private String authHeader;

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

    @Autowired
    private StatusCtrl statusCtrl;

    @PostMapping("/start")
    public Result<Void> start(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        statusCtrl.setSubjectTopicThreadNum(2);
        statusCtrl.setPaperSourceThreadNum(2);
        statusCtrl.setMainInfoThreadNum(2);
        statusCtrl.setJournalThreadNum(2);
        statusCtrl.setResearcherThreadNum(2);
        if (statusCtrl.start())
            return result;
        return result.withFailure("Has been running");
    }

    @PostMapping("/stop")
    public Result<Void> stop(@RequestHeader(name = "Auth") String auth)  {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        if (!statusCtrl.isRunning())
            return result.withFailure("Has stopped");
        statusCtrl.stop();
        return result;
    }

    @GetMapping("/status")
    public Result<Schedule> getStatus(@RequestHeader(name = "Auth") String auth) {
        Result<Schedule> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        return result.withData(statusCtrl.getStatus());
    }

    @PostMapping("/setting")
    public Result<Void> setting(@RequestHeader(name = "Auth") String auth,
                                @RequestBody List<String> keywords) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        if (statusCtrl.isRunning())
            return result.withFailure("Has been running");
        statusCtrl.setKeywords(keywords);
        return result;
    }
}
