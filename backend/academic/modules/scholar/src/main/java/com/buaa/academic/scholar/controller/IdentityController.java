package com.buaa.academic.scholar.controller;

import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.CertificationApp;
import com.buaa.academic.model.application.ClaimApp;
import com.buaa.academic.model.application.ModificationApp;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.service.ApplicationService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/identity")
@Validated
@Api(tags = "学者身份相关")
public class IdentityController {
    @Autowired
    ApplicationService<CertificationApp> certifyAppService;

    @PostMapping("/certify")
    @ApiIgnore
    public Result<Void> certify(@RequestHeader(value = "Auth") String userId,
                                @RequestBody ApplicationInfo<CertificationApp> ctfApp) {
        Result<Void> result = new Result<>();
        if (certifyAppService.submitApp(ctfApp, userId, "学者认证"))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

    @Autowired
    ApplicationService<ClaimApp> claimAppService;

    @PostMapping("/claim")
    @ApiIgnore
    public Result<Void> claim(@RequestHeader(value = "Auth") String userId,
                              @RequestBody ApplicationInfo<ClaimApp> claimApp) {
        Result<Void> result = new Result<>();
        if (claimAppService.submitApp(claimApp, userId, "门户认领"))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

    @Autowired
    ApplicationService<ModificationApp> modificationAppService;

    @PostMapping("/modify")
    @ApiIgnore
    public Result<Void> modify(@RequestHeader(value = "Auth") String userId,
                               @RequestBody ApplicationInfo<ModificationApp> mdfApp) {
        Result<Void> result = new Result<>();
        if (modificationAppService.submitApp(mdfApp, userId, "学者信息修改"))
            return result;
        else
            return result.withFailure(ExceptionType.INVALID_PARAM);
    }

}
