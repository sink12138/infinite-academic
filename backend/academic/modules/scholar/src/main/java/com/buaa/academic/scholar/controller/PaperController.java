package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.PaperAdd;
import com.buaa.academic.model.application.PaperEdit;
import com.buaa.academic.model.application.PaperRemove;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.client.ResourceClient;
import com.buaa.academic.scholar.service.ApplicationService;
import com.buaa.academic.scholar.utils.ExistenceCheck;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paper")
@Validated
@Api(tags = "文章相关申请")
public class PaperController {

    @Autowired
    ApplicationService<PaperRemove> paperRemoveAppService;

    @Autowired
    ApplicationService<PaperAdd> paperAddService;

    @Autowired
    ExistenceCheck existenceCheck;

    @Autowired
    ResourceClient resourceClient;

    @ApiOperation(value = "添加论文", notes = "添加已发表的文章或在本网站首发文章")
    @PostMapping("/add")
    public Result<Void> addPaper(@RequestHeader(value = "Auth") String userId,
                                 @RequestBody ApplicationInfo<PaperAdd> paper) {
        Result<Void> result = new Result<>();
        if (!existenceCheck.paperAddCheck(paper.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (paper.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(paper.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!paperAddService.submitAppWithCtf(paper, userId, ApplicationType.NEW_PAPER)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        return result;
    }

    @Autowired
    ApplicationService<PaperEdit> paperEditService;

    @ApiOperation(value = "修改论文", notes = "修改论文")
    @PostMapping("/edit")
    public Result<Void> editPaper(@RequestHeader(value = "Auth") String userId,
                                  @RequestBody ApplicationInfo<PaperEdit> paper) {
        Result<Void> result = new Result<>();
        if (paper.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(paper.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!existenceCheck.paperEditCheck(paper.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (!paperEditService.submitAppWithCtf(paper, userId, ApplicationType.EDIT_PAPER)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        return new Result<>();
    }

    @PostMapping("/remove")
    @ApiOperation(value = "移除论文")
    public Result<Void> removePaper(@RequestHeader(value = "Auth") String userId,
                                    @RequestBody ApplicationInfo<PaperRemove> paper) {
        Result<Void> result = new Result<>();
        if (paper.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(paper.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!existenceCheck.paperRemoveCheck(paper.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        paperRemoveAppService.submitAppWithoutCtf(paper, userId, ApplicationType.REMOVE_PAPER);
        return result;
    }

}
