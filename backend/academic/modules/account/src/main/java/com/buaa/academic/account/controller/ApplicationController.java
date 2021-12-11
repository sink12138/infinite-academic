package com.buaa.academic.account.controller;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.account.model.ApplicationPage;
import com.buaa.academic.account.repository.ApplicationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;

@RestController
@RequestMapping("/application")
@Validated
@Api(tags = "申请相关")
public class ApplicationController {
    @Autowired
    ApplicationRepository appRepository;

    @ApiOperation(value = "查看所有申请")
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页数"),
            @ApiImplicitParam(name = "size", value = "一页数量"),
            @ApiImplicitParam(name = "type", value = "申请类型, 不传则默认返回所有类型", example = "学者认证"),
            @ApiImplicitParam(name = "status", value = "处理状态, 不传则默认返回所有状态", example = "审核中")})
    public Result<ApplicationPage> getAllApp(@RequestHeader(value = "Auth") String userId,
                                             @RequestParam(value = "page") @PositiveOrZero int page,
                                             @RequestParam(value = "size") @Range(min = 1, max = 30) int size,
                                             @RequestParam(value = "type", required = false)  ApplicationType type,
                                             @RequestParam(value = "status", required = false) StatusType status) {
        Result<ApplicationPage> result = new Result<>();
        ApplicationPage applicationPage = new ApplicationPage();
        Page<Application> applicationSearchPage;
        Pageable pageable = PageRequest.of(page, size);
        if (type == null && status == null)
            applicationSearchPage = appRepository.findByUserIdEqualsOrderByTimeDesc(userId, pageable);
        else if (type != null && status != null)
            applicationSearchPage = appRepository.findByUserIdEqualsAndStatusEqualsAndTypeEqualsOrderByTimeDesc(userId, status.getDescription(), type.getDescription(), pageable);
        else if (type != null)
            applicationSearchPage = appRepository.findByUserIdEqualsAndTypeEqualsOrderByTimeDesc(userId, type.getDescription(), pageable);
        else
            applicationSearchPage = appRepository.findByUserIdEqualsAndStatusEqualsOrderByTimeDesc(userId, status.getDescription(), pageable);
        applicationPage.setPageCount(applicationSearchPage.getTotalPages());
        ArrayList<Application> applications = new ArrayList<>();
        applicationSearchPage.forEach(applications::add);
        applicationPage.setApplications(applications);
        return result.withData(applicationPage);
    }
}
