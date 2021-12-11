package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.CertificationApp;
import com.buaa.academic.model.application.ClaimApp;
import com.buaa.academic.model.application.ModificationApp;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.client.AccountClient;
import com.buaa.academic.scholar.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Validated
@Api(tags = "身份相关申请")
public class IdentityController {
    @Autowired
    ElasticsearchRestTemplate template;

    @Autowired
    ApplicationService<CertificationApp> certifyAppService;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/certify/submit")
    @ApiOperation(value = "学者身份认证申请")
    public Result<Void> certifySubmit(@RequestHeader(value = "Auth") String userId,
                                      @RequestBody ApplicationInfo<CertificationApp> ctfApp) {
        Result<Void> result = new Result<>();
        Object target = redisTemplate.opsForValue().get(ctfApp.getApplication().getCode());
        if (target == null)
            return result.withFailure("验证码已失效");
        redisTemplate.delete(ctfApp.getApplication().getCode());
        if (certifyAppService.submitAppWithCtf(ctfApp, userId, ApplicationType.CERTIFICATION))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

    @Autowired
    AccountClient accountClient;

    @PostMapping("/certify")
    @ApiOperation(value = "学者认证")
    public Result<Void> certify(@RequestHeader(value = "Auth") String userId,
                                @RequestParam(value = "email") String email) {
        return accountClient.sendVerifyCode(userId, email, "学者认证");
    }

    @Autowired
    ApplicationService<ClaimApp> claimAppService;

    @PostMapping("/claim")
    @ApiOperation(value = "门户认领申请")
    public Result<Void> claim(@RequestHeader(value = "Auth") String userId,
                              @RequestBody ApplicationInfo<ClaimApp> claimApp) {
        Result<Void> result = new Result<>();
        if (claimAppService.submitAppWithCtf(claimApp, userId, ApplicationType.CLAIM))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

    @Autowired
    ApplicationService<ModificationApp> modificationAppService;

    @PostMapping("/modify")
    @ApiOperation(value = "学者信息修改申请")
    public Result<Void> modify(@RequestHeader(value = "Auth") String userId,
                               @RequestBody ApplicationInfo<ModificationApp> mdfApp) {
        Result<Void> result = new Result<>();
        if (modificationAppService.submitAppWithCtf(mdfApp, userId, ApplicationType.MODIFICATION))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }
}
