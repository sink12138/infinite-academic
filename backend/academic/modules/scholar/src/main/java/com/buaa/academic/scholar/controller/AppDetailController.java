package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.model.application.*;
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
import java.util.Base64;

@RestController
@RequestMapping("/detail")
@Api(tags = "申请详细内容")
public class AppDetailController {
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ElasticsearchRestTemplate template;

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

    @GetMapping("/remove")
    @ApiOperation(value = "移除文章申请具体信息")
    public Result<PaperRemoveApp> getPaperRmApp(@RequestHeader(value = "Auth") String auth,
                                                @RequestParam(value = "appId") String appId) {
        Result<PaperRemoveApp> result = new Result<>();
        if (!isAdminHeader(auth)) {
            Application application = template.get(appId, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (!application.getId().equals(auth))
                return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        return result.withData((PaperRemoveApp) redisTemplate.opsForValue().get(appId));
    }

    @GetMapping("/add")
    @ApiOperation(value = "添加文章申请具体信息")
    public Result<PaperAddApp> getPaperAddApp(@RequestHeader(value = "Auth") String auth,
                                              @RequestParam(value = "appId") String appId) {
        Result<PaperAddApp> result = new Result<>();
        if (!isAdminHeader(auth)) {
            Application application = template.get(appId, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (!application.getId().equals(auth))
                return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        return result.withData((PaperAddApp) redisTemplate.opsForValue().get(appId));
    }

    @GetMapping("/certify")
    @ApiOperation(value = "认证申请具体信息")
    public Result<CertificationApp> getCtfAppDetail(@RequestHeader(value = "Auth") String auth,
                                                    @RequestParam(value = "appId") String appId) {
        Result<CertificationApp> result = new Result<>();
        if (!isAdminHeader(auth)) {
            Application application = template.get(appId, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (!application.getId().equals(auth))
                return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        CertificationApp certificationApp = (CertificationApp) redisTemplate.opsForValue().get(appId);
        return result.withData(certificationApp);
    }

    @GetMapping("/claim")
    @ApiOperation(value = "认领申请门户具体信息")
    public Result<ClaimApp> getClaimAppDetail(@RequestHeader(value = "Auth") String auth,
                                              @RequestParam(value = "appId") String appId) {
        Result<ClaimApp> result = new Result<>();
        if (!isAdminHeader(auth)) {
            Application application = template.get(appId, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (!application.getId().equals(auth))
                return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        ClaimApp claimApp = (ClaimApp) redisTemplate.opsForValue().get(appId);
        return result.withData(claimApp);
    }

    @GetMapping("/modify")
    @ApiOperation(value = "修改学者信息申请具体信息")
    public Result<ModificationApp> getMdfAppDetail(@RequestHeader(value = "Auth") String auth,
                                                   @RequestParam(value = "appId") String appId) {
        Result<ModificationApp> result = new Result<>();
        if (!isAdminHeader(auth)) {
            Application application = template.get(appId, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (!application.getId().equals(auth))
                return result.withFailure(ExceptionType.UNAUTHORIZED);
        }
        ModificationApp mdfApp = (ModificationApp) redisTemplate.opsForValue().get(appId);
        return result.withData(mdfApp);
    }
}
