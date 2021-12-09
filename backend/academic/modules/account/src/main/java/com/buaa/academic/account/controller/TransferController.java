package com.buaa.academic.account.controller;

import com.buaa.academic.account.model.TransferInfo;
import com.buaa.academic.account.model.ApplicationPage;
import com.buaa.academic.account.repository.ApplicationRepository;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.model.Application.TransferApplication;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/transfer")
@Api(value = "专利转让")
public class TransferController {
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ElasticsearchRestTemplate template;

    @PostMapping("/submit")
    public Result<Void> transferSubmit(@RequestHeader(value = "Auth") String userId,
                                       @RequestBody TransferInfo transferInfo) {
        Application application = new Application();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        application.setUserId(userId);
        application.setEmail(transferInfo.getContactEmail());
        application.setTime(simpleDateFormat.format(date));
        application.setType("专利转让");
        application.setFileToken(transferInfo.getFileToken());
        applicationRepository.save(application);
        redisTemplate.opsForValue().set(application.getId(), transferInfo.getTransferApplication());
        return new Result<>();
    }

    @GetMapping("/list")
    public Result<ApplicationPage> getTransfer(@RequestHeader(value = "Auth") String userId,
                                               @RequestParam(value = "page") Integer page,
                                               @RequestParam(value = "size") Integer size,
                                               @RequestParam(value = "status", required = false) String status) {
        ApplicationPage applicationPage = new ApplicationPage();
        SearchPage<Application> applicationSearchPage;
        if (status == null) {
            applicationSearchPage = applicationRepository.findByUserIdEquals(userId, PageRequest.of(page, size));
        } else {
            applicationSearchPage = applicationRepository.findByUserIdEqualsAndStatusEquals(userId, status, PageRequest.of(page, size));
        }
        applicationPage.setPageCount(applicationSearchPage.getTotalPages());
        ArrayList<Application> applications = new ArrayList<>();
        applicationSearchPage.getSearchHits().forEach(applicationSearchHit ->
            applications.add(applicationSearchHit.getContent())
        );
        applicationPage.setApplications(applications);
        return new Result<ApplicationPage>().withData(applicationPage);
    }

    @GetMapping("/details")
    public Result<TransferApplication> getTransferDetails(@RequestHeader(value = "Auth") String userId,
                                                          @RequestParam(value = "applicationId") String appId) {
        Result<TransferApplication> result = new Result<>();
        Application application = template.get(appId, Application.class);
        if (application == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (!application.getUserId().equals(userId))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        return result.withData((TransferApplication) redisTemplate.opsForValue().get(appId));
    }
}
