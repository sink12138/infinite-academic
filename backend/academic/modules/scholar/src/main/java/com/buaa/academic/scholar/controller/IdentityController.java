package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.Certification;
import com.buaa.academic.model.application.Claim;
import com.buaa.academic.model.application.Modification;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.client.AccountClient;
import com.buaa.academic.scholar.client.ResourceClient;
import com.buaa.academic.scholar.service.ApplicationService;
import com.buaa.academic.scholar.utils.ExistenceCheck;
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
    private ApplicationService<Certification> certifyAppService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private ApplicationService<Claim> claimAppService;

    @Autowired
    private ApplicationService<Modification> modificationAppService;

    @Autowired
    private ExistenceCheck existenceCheck;

    @Autowired
    private ResourceClient resourceClient;

    @PostMapping("/certify/code")
    @ApiOperation(value = "学者认证获取验证码")
    public Result<Void> certifyCode(@RequestHeader(value = "Auth") String userId,
                                @RequestParam(value = "email") String email) {
        return accountClient.sendVerifyCode(userId, email, "学者认证");
    }

    @PostMapping("/certify")
    @ApiOperation(value = "学者身份认证申请")
    public Result<Void> certifyCommit(@RequestHeader(value = "Auth") String userId,
                                      @RequestBody ApplicationInfo<Certification> ctfApp) {
        Result<Void> result = new Result<>();
        Object target = redisTemplate.opsForValue().get(ctfApp.getContent().getCode());
        if (target == null)
            return result.withFailure("验证码已失效");
        if (!existenceCheck.certificationCheck(ctfApp.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        redisTemplate.delete(ctfApp.getContent().getCode());
        ctfApp.getContent().setCode(null);
        if (ctfApp.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(ctfApp.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (certifyAppService.submitAppWithCtf(ctfApp, userId, ApplicationType.CERTIFICATION))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

    @PostMapping("/claim")
    @ApiOperation(value = "门户认领申请")
    public Result<Void> claim(@RequestHeader(value = "Auth") String userId,
                              @RequestBody ApplicationInfo<Claim> claimApp) {
        Result<Void> result = new Result<>();
        if (claimApp.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(claimApp.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!existenceCheck.claimCheck(claimApp.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (claimAppService.submitAppWithCtf(claimApp, userId, ApplicationType.CLAIM))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

    @PostMapping("/modify")
    @ApiOperation(value = "学者信息修改申请")
    public Result<Void> modify(@RequestHeader(value = "Auth") String userId,
                               @RequestBody ApplicationInfo<Modification> mdfApp) {
        Result<Void> result = new Result<>();
        if (mdfApp.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(mdfApp.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!existenceCheck.modificationCheck(mdfApp.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (modificationAppService.submitAppWithCtf(mdfApp, userId, ApplicationType.MODIFICATION))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }
}
