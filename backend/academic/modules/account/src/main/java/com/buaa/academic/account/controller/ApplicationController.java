package com.buaa.academic.account.controller;

import com.buaa.academic.account.model.ApplicationDetails;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import com.buaa.academic.model.application.*;
import com.buaa.academic.model.exception.ExceptionType;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;

@RestController
@RequestMapping("/application")
@Validated
@Api(tags = "申请相关")
public class ApplicationController {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;
    
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("time")));
        if (type == null && status == null)
            applicationSearchPage = applicationRepository.findByUserId(userId, pageable);
        else if (type != null && status != null)
            applicationSearchPage = applicationRepository.findByUserIdAndStatusAndType(userId, status.getDescription(), type.getDescription(), pageable);
        else if (type != null)
            applicationSearchPage = applicationRepository.findByUserIdAndType(userId, type.getDescription(), pageable);
        else
            applicationSearchPage = applicationRepository.findByUserIdAndStatus(userId, status.getDescription(), pageable);
        applicationPage.setPageCount(applicationSearchPage.getTotalPages());
        ArrayList<Application> applications = new ArrayList<>();
        applicationSearchPage.forEach(applications::add);
        applicationPage.setApplications(applications);
        return result.withData(applicationPage);
    }

    @GetMapping("/details/{id}")
    @ApiOperation(value = "查看申请详细信息", notes = "详细内容的结构还在设计中，可参考其他几个临时的详细信息示例API")
    @ApiImplicitParam(name = "id", value = "申请的ID")
    public Result<ApplicationDetails<Object>> details(@RequestHeader(name = "Auth") String userId,
                                                      @PathVariable(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<ApplicationDetails<Object>> result = new Result<>();
        Application application = elasticTemplate.get(id, Application.class);
        if (application == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (!application.getId().equals(userId))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        Object content = redisTemplate.opsForValue().get(id);
        return result.withData(new ApplicationDetails<>(application, content));
    }

    @GetMapping("/details/certification")
    @ApiOperation(value = "学者认证详细信息示例", notes = "本接口仅用于演示学者认证申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<Certification>> certificationDetails() {
        return new Result<ApplicationDetails<Certification>>()
                .withFailure("本接口仅用于演示学者认证申请的响应体结构，请申请/details/{id}接口");
    }

    @GetMapping("/details/claim")
    @ApiOperation(value = "门户认领详细信息示例", notes = "本接口仅用于演示门户认领申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<Claim>> claimDetails() {
        return new Result<ApplicationDetails<Claim>>()
                .withFailure("本接口仅用于演示门户认领申请的响应体结构，请申请/details/{id}接口");
    }

    @GetMapping("/details/modification")
    @ApiOperation(value = "修改门户详细信息示例", notes = "本接口仅用于演示修改门户申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<Modification>> modificationDetails() {
        return new Result<ApplicationDetails<Modification>>()
                .withFailure("本接口仅用于演示修改门户申请的响应体结构，请申请/details/{id}接口");
    }

    @GetMapping("/details/paper/add")
    @ApiOperation(value = "添加论文详细信息示例", notes = "本接口仅用于演示添加论文申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<PaperAdd>> addPaperDetails() {
        return new Result<ApplicationDetails<PaperAdd>>()
                .withFailure("本接口仅用于演示添加论文申请的响应体结构，请申请/details/{id}接口");
    }

    @GetMapping("/details/paper/edit")
    @ApiOperation(value = "修改论文详细信息示例", notes = "本接口仅用于演示修改论文申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<PaperEdit>> editPaper() {
        return new Result<ApplicationDetails<PaperEdit>>()
                .withFailure("本接口仅用于演示修改论文申请的响应体结构，请申请/details/{id}接口");
    }

    @GetMapping("/details/paper/remove")
    @ApiOperation(value = "移除论文详细信息示例", notes = "本接口仅用于演示移除论文申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<PaperRemove>> removePaper() {
        return new Result<ApplicationDetails<PaperRemove>>()
                .withFailure("本接口仅用于演示移除论文申请的响应体结构，请申请/details/{id}接口");
    }

    @GetMapping("/details/transfer")
    @ApiOperation(value = "专利转让详细信息示例", notes = "本接口仅用于演示专利转让申请的响应体结构，请申请/details/{id}接口")
    public Result<ApplicationDetails<Transfer>> transfer() {
        return new Result<ApplicationDetails<Transfer>>()
                .withFailure("本接口仅用于演示专利转让申请的响应体结构，请申请/details/{id}接口");
    }

}
