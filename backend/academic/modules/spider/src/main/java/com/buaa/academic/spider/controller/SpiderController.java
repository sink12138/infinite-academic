package com.buaa.academic.spider.controller;

import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.spider.service.CrawlService;
import com.buaa.academic.spider.util.StatusCtrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Validated
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
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidHeader(String auth) {
        return auth.equals(authHeader);
    }

    @Autowired
    private StatusCtrl statusCtrl;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private CrawlService crawlService;

    @PostMapping("/start")
    public Result<Void> start(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        statusCtrl.setPapersInitThreadNum(0);
        statusCtrl.setPaperMainInfoNum(0);
        statusCtrl.setPaperSourceThreadNum(0);
        statusCtrl.setResearcherThreadNum(0);
        statusCtrl.setInterestsThreadNum(0);

        statusCtrl.setJournalThreadNum(0);
        statusCtrl.setSubjectTopicThreadNum(0);
        statusCtrl.setPatentsInitThreadNum(3);
        statusCtrl.setPatentMainInfoNum(6);
        if (statusCtrl.start())
            return result;
        return result.withFailure("Has been running");
    }

    @PostMapping("/stop")
    public Result<Void> stop(@RequestHeader(name = "Auth") String auth) {
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
        StatusCtrl.paperKeywordQueue.addAll(keywords);
        return result;
    }

    @PostMapping("/crawlAuId")
    public Result<Void> crawlAuId(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        statusCtrl.setPapersInitThreadNum(3);
        statusCtrl.setPaperSourceThreadNum(2);
        statusCtrl.setPaperMainInfoNum(5);
        statusCtrl.setResearcherThreadNum(7);
        statusCtrl.setSubjectTopicThreadNum(1);
        statusCtrl.setJournalThreadNum(1);
        statusCtrl.setInterestsThreadNum(1);
        if (statusCtrl.fixResearcherId())
            return result;
        return result.withFailure("Has been running");
    }

    @PostMapping("/crawlWithUrl")
    public Result<Void> crawlWithUrl(@RequestHeader(name = "Auth") String userId,
                                     @RequestParam(name = "url") @NotBlank String url) {
        Result<Void> result = new Result<>();
        if (!url.startsWith("https://kns.cnki.net/kcms/detail") && !url.startsWith("https://d.wanfangdata.com.cn/")) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        if (!template.exists(userId, User.class))
            return result.withFailure(ExceptionType.NOT_FOUND);
        crawlService.crawlWithUrl(url, userId);
        return result;
    }

    @PostMapping("/crawlWithTitle")
    public Result<Void> crawlWithTitle(@RequestHeader(name = "Auth") String userId,
                                       @RequestParam(name = "title") @NotBlank String title) {
        Result<Void> result = new Result<>();
        if (!template.exists(userId, User.class))
            return result.withFailure(ExceptionType.NOT_FOUND);
        crawlService.crawlWithTitle(title, userId);
        return result;
    }
}
