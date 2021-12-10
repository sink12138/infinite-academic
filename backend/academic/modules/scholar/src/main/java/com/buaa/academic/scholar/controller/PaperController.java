package com.buaa.academic.scholar.controller;

import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.PaperAddApp;
import com.buaa.academic.model.application.PaperRemoveApp;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.service.ApplicationService;
import com.buaa.academic.tool.validator.AllowValues;
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
    ApplicationService<PaperRemoveApp> paperRemoveAppService;

    @PostMapping("/remove")
    @ApiOperation(value = "移除文章申请")
    public Result<Void> removePaper(@RequestHeader(value = "Auth") String userId,
                                    @RequestBody ApplicationInfo<PaperRemoveApp> paperRemoveAppInfo) {
        paperRemoveAppService.submitAppWithoutCtf(paperRemoveAppInfo, userId, "文章删除");
        return new Result<>();
    }

    @Autowired
    ApplicationService<PaperAddApp> paperAddService;

    @ApiOperation(value = "增加文章", notes = "添加已发表的文章或在本网站首发文章" +
            "<br>首发文章必传fileToken和application; " +
            "添加已发表文章webSiteLink和fileToken必需有其中之一，支持爬虫的网站可不传application</br>")
    @PostMapping("/{operation}")
    public Result<Void> addPaper(@RequestHeader(value = "Auth") String userId,
                                 @RequestBody ApplicationInfo<PaperAddApp> paper,
                                 @PathVariable("operation") @AllowValues({"add", "publish"}) String operation) {
        Result<Void> result = new Result<>();
        String type;
        if (operation.equals("publish")) {
            if (paper.getFileToken() == null || paper.getApplication() == null)
                return result.withFailure(ExceptionType.INVALID_PARAM);
            type = "发表文章";
        } else
            type = "添加文章";
        if (!paperAddService.submitAppWithCtf(paper, userId, type)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        return result;
    }
}
