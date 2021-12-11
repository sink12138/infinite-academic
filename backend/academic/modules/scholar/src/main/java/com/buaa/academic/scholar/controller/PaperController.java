package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.PaperAdd;
import com.buaa.academic.model.application.PaperRemove;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.service.ApplicationService;
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

    @ApiOperation(value = "添加论文", notes = "添加已发表的文章或在本网站首发文章")
    @PostMapping("/add")
    public Result<Void> addPaper(@RequestHeader(value = "Auth") String userId,
                                 @RequestBody ApplicationInfo<PaperAdd> paper) {
        Result<Void> result = new Result<>();
        if (!paperAddService.submitAppWithCtf(paper, userId, ApplicationType.NEW_PAPER)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        return result;
    }

    @ApiOperation(value = "修改论文", notes = "添加已发表的文章或在本网站首发文章")
    @PostMapping("/edit")
    public Result<Void> editPaper(@RequestHeader(value = "Auth") String userId,
                                  @RequestBody ApplicationInfo<PaperAdd> paper) {
        // TODO: 2021/12/11 Finish this API
        return new Result<>();
    }

    @PostMapping("/remove")
    @ApiOperation(value = "移除论文")
    public Result<Void> removePaper(@RequestHeader(value = "Auth") String userId,
                                    @RequestBody ApplicationInfo<PaperRemove> paperRemoveAppInfo) {
        paperRemoveAppService.submitAppWithoutCtf(paperRemoveAppInfo, userId, ApplicationType.REMOVE_PAPER);
        return new Result<>();
    }

}
