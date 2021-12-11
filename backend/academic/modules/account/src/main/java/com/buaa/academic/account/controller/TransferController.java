package com.buaa.academic.account.controller;

import com.buaa.academic.account.repository.ApplicationRepository;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.TransferApp;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@RestController
@Api(value = "专利转让相关")
public class TransferController {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ElasticsearchRestTemplate template;

    @PostMapping("/transfer")
    @ApiOperation(value = "专利转让申请提交")
    public Result<Void> transferSubmit(@RequestHeader(value = "Auth") String userId,
                                       @RequestBody ApplicationInfo<TransferApp> transferInfo) {
        // todo 检查文件是否成功上传
        Application application = new Application();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        application.setUserId(userId);
        application.setEmail(transferInfo.getEmail());
        application.setTime(simpleDateFormat.format(date));
        application.setType(ApplicationType.TRANSFER);
        application.setStatus(StatusType.UNDER_REVIEW);
        application.setFileToken(transferInfo.getFileToken());
        applicationRepository.save(application);
        TransferApp transferApp = transferInfo.getApplication();
        redisTemplate.opsForValue().set(application.getId(), transferApp);
        return new Result<>();
    }

    private static String authHeader;

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
    private boolean isAdminHeader(String auth) {
        return auth.equals(authHeader);
    }

    @GetMapping("/detail/transfer")
    @ApiOperation(value = "专利转让申请具体信息")
    public Result<TransferApp> getTransferDetails(@RequestHeader(value = "Auth") String auth,
                                                  @RequestParam(value = "applicationId") String appId) {
        Result<TransferApp> result = new Result<>();
        if (!isAdminHeader(auth)) {
            Application application = template.get(appId, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (!application.getUserId().equals(auth))
                return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        return result.withData((TransferApp) redisTemplate.opsForValue().get(appId));
    }
}
