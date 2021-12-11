package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.CertificationApp;
import com.buaa.academic.model.application.ClaimApp;
import com.buaa.academic.model.application.ModificationApp;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Api(tags = "身份相关申请")
public class IdentityController {
    @Autowired
    ElasticsearchRestTemplate template;

    @Autowired
    ApplicationService<CertificationApp> certifyAppService;

    @PostMapping("/certify")
    @ApiOperation(value = "学者身份认证申请")
    public Result<Void> certify(@RequestHeader(value = "Auth") String userId,
                                @RequestBody ApplicationInfo<CertificationApp> ctfApp) {
        Result<Void> result = new Result<>();
        if (certifyAppService.submitAppWithCtf(ctfApp, userId, ApplicationType.CERTIFICATION))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
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
