package com.buaa.academic.scholar.controller;

import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.CertificationApp;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.service.ApplicationService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.annotation.Resource;

@RestController
@RequestMapping("/identity")
@Validated
@Api(tags = "学者身份相关")
public class IdentityController {
    @Autowired
    ApplicationService applicationService;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/certify")
    @ApiIgnore
    public Result<Void> certify(@RequestHeader(value = "Auth") String userId,
                                @RequestBody ApplicationInfo<CertificationApp> ctfApp) {
        Result<Void> result = new Result<>();
        String appId;
        CertificationApp certificationApp = ctfApp.getApplication();
        String type = "学者认证";
        if (ctfApp.getFileToken() != null) {
            appId = applicationService.saveAppWithFile(userId, ctfApp.getContactEmail(), ctfApp.getFileToken(), type);
        } else if (ctfApp.getWebsiteLink() != null) {
            appId = applicationService.saveAppWithFile(userId, ctfApp.getContactEmail(), ctfApp.getFileToken(), type);
        } else
            return result.withFailure(ExceptionType.INVALID_PARAM);
        redisTemplate.opsForValue().set(appId, certificationApp);
        return result;
    }

    @PostMapping("/claim")
    @ApiIgnore
    public Result<Void> claim() {
        return new Result<>();
    }

    @PostMapping("/modify")
    @ApiIgnore
    public Result<Void> modify() {
        return new Result<>();
    }

}
