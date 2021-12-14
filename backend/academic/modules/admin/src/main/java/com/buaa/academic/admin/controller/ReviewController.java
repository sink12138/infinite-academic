package com.buaa.academic.admin.controller;

import com.buaa.academic.admin.dao.ApplicationRepository;
import com.buaa.academic.admin.model.ApplicationDetails;
import com.buaa.academic.admin.model.ListPage;
import com.buaa.academic.admin.service.AuthValidator;
import com.buaa.academic.admin.service.MessageService;
import com.buaa.academic.admin.service.ReviewService;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import com.buaa.academic.model.application.*;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Length;
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
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/review")
@Validated
@Api(tags = "后台审核相关")
public class ReviewController {

    @Autowired
    private AuthValidator authValidator;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MessageService messageService;

    private static final Object REVIEW_LOCK = new Object();

    @GetMapping("/list")
    @ApiOperation(value = "查看所有申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码，从0开始"),
            @ApiImplicitParam(name = "size", value = "每页显示数量，最多30"),
            @ApiImplicitParam(
                    name = "type", example = "学者认证",
                    value = "申请类型，不传则默认返回所有类型。可用值：学者认证/门户认领/门户信息修改/添加论文/下架论文/修改论文信息/专利转让"),
            @ApiImplicitParam(
                    name = "status", example = "审核中",
                    value = "处理状态，不传则默认返回所有状态。可用值：审核中/审核通过/审核不通过")})
    public Result<ListPage<Application>> list(@RequestHeader(name = "Auth") String auth,
                                 @RequestParam(name = "page") @PositiveOrZero int page,
                                 @RequestParam(name = "size") @Range(min = 1, max = 30) int size,
                                 @RequestParam(name = "type", required = false) ApplicationType type,
                                 @RequestParam(name = "status", required = false) StatusType status) {
        Result<ListPage<Application>> result = new Result<>();
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("time")));
        Page<Application> searchPage;
        if (type != null && status != null) {
            searchPage = applicationRepository.findByTypeAndStatus(type.getDescription(), status.getDescription(), pageable);
        }
        else if (type != null) {
            searchPage = applicationRepository.findByType(type.getDescription(), pageable);
        }
        else if (status != null) {
            searchPage = applicationRepository.findByStatus(status.getDescription(), pageable);
        }
        else {
            searchPage = applicationRepository.findAll(pageable);
        }
        ListPage<Application> applicationPage = new ListPage<>();
        searchPage.forEach(applicationPage::add);
        applicationPage.setPageCount(searchPage.getTotalPages());
        return result.withData(applicationPage);
    }

    @GetMapping("/details/{id}")
    @ApiOperation(value = "查看申请详细信息", notes = "详细内容的结构可参考/account/application/details下几个临时的详细信息API")
    @ApiImplicitParam(name = "id", value = "申请的ID")
    public Result<ApplicationDetails<Object>> details(@RequestHeader(name = "Auth") String auth,
                                                      @PathVariable(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<ApplicationDetails<Object>> result = new Result<>();
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        Application application = elasticTemplate.get(id, Application.class);
        if (application == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        Object content = redisTemplate.opsForValue().get(id);
        return result.withData(new ApplicationDetails<>(application, content));
    }

    @PostMapping("/reject")
    @ApiOperation(value = "拒绝申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "申请的ID"),
            @ApiImplicitParam(name = "reason", value = "拒绝的原因，非必填，最多300字")})
    public Result<Void> reject(@RequestHeader(name = "Auth") String auth,
                               @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id,
                               @RequestParam(name = "reason", required = false) @Length(max = 300) String reason) {
        Result<Void> result = new Result<>();
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        Application application;
        synchronized (REVIEW_LOCK) {
            application = elasticTemplate.get(id, Application.class);
            if (application == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            if (reason == null)
                reason = "无";
            StatusType status = application.getStatus();
            if (StatusType.PASSED.equals(status))
                return result.withFailure("已通过的申请不能拒绝");
            else if (StatusType.NOT_PASSED.equals(status))
                return result;
            application.setStatus(StatusType.NOT_PASSED);
            elasticTemplate.save(application);
        }
        messageService.sendRejectMessage(application, reason);
        return result;
    }

    @PostMapping("/accept/certification")
    @ApiOperation(value = "接受学者认证申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是学者认证类型")
    public Result<Void> acceptCertification(@RequestHeader(name = "Auth") String auth,
                                            @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.CERTIFICATION.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            Certification certification = (Certification) redisTemplate.opsForValue().get(id);
            if (certification == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            User user = Objects.requireNonNull(elasticTemplate.get(application.getUserId(), User.class));
            application.setStatus(StatusType.PASSED);
            elasticTemplate.save(application);
            Claim claim = certification.getClaim();
            if (claim == null) {
                reviewService.createResearcher(user, certification.getCreate());
            }
            else {
                List<String> portals = claim.getPortals();
                user.setResearcherId(portals.get(0));
                reviewService.mergeResearcher(portals.get(0), portals.subList(1, portals.size()));
            }
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

    @PostMapping("/accept/claim")
    @ApiOperation(value = "接受门户认领申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是门户认领类型")
    public Result<Void> acceptClaim(@RequestHeader(name = "Auth") String auth,
                                    @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.CLAIM.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            Claim claim = (Claim) redisTemplate.opsForValue().get(id);
            if (claim == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            User user = Objects.requireNonNull(elasticTemplate.get(application.getUserId(), User.class));
            String researcherId = Objects.requireNonNull(user.getResearcherId());
            application.setStatus(StatusType.PASSED);
            elasticTemplate.save(application);
            reviewService.mergeResearcher(researcherId, claim.getPortals());
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

    @PostMapping("/accept/modification")
    @ApiOperation(value = "接受门户修改申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是门户修改类型")
    public Result<Void> acceptModification(@RequestHeader(name = "Auth") String auth,
                                           @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.MODIFICATION.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            Modification modification = (Modification) redisTemplate.opsForValue().get(id);
            if (modification == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            User user = Objects.requireNonNull(elasticTemplate.get(application.getUserId(), User.class));
            String researcherId = Objects.requireNonNull(user.getResearcherId());
            application.setStatus(StatusType.PASSED);
            elasticTemplate.save(application);
            reviewService.modifyResearcher(researcherId, modification.getInfo());
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

    @PostMapping("/accept/paper/add")
    @ApiOperation(value = "接受添加论文申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是添加论文类型")
    public Result<Void> acceptPaperAdd(@RequestHeader(name = "Auth") String auth,
                                       @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.NEW_PAPER.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            PaperAdd paperAdd = (PaperAdd) redisTemplate.opsForValue().get(id);
            if (paperAdd == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            reviewService.savePaper(null, paperAdd.getAdd());
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

    @PostMapping("/accept/paper/edit")
    @ApiOperation(value = "接受修改论文申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是修改类型")
    public Result<Void> acceptPaperEdit(@RequestHeader(name = "Auth") String auth,
                                        @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.EDIT_PAPER.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            PaperEdit paperEdit = (PaperEdit) redisTemplate.opsForValue().get(id);
            if (paperEdit == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            reviewService.savePaper(paperEdit.getPaperId(), paperEdit.getEdit());
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

    @PostMapping("/accept/paper/remove")
    @ApiOperation(value = "接受移除论文申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是移除论文类型")
    public Result<Void> acceptPaperRemove(@RequestHeader(name = "Auth") String auth,
                                          @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.REMOVE_PAPER.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            PaperRemove paperRemove = (PaperRemove) redisTemplate.opsForValue().get(id);
            if (paperRemove == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            application.setStatus(StatusType.PASSED);
            elasticTemplate.save(application);
            reviewService.removePaper(paperRemove.getPaperId());
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

    @PostMapping("/accept/transfer")
    @ApiOperation(value = "接受专利转让申请")
    @ApiImplicitParam(name = "id", value = "申请的ID，该申请必须是专利转让类型")
    public Result<Void> acceptPatentTransfer(@RequestHeader(name = "Auth") String auth,
                                             @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z]{20}$") String id) {
        Result<Void> result = new Result<>();

        // Authority
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        synchronized (REVIEW_LOCK) {
            // Existence
            Application application = elasticTemplate.get(id, Application.class);
            if (application == null || !ApplicationType.TRANSFER.equals(application.getType()))
                return result.withFailure(ExceptionType.NOT_FOUND);
            Transfer transfer = (Transfer) redisTemplate.opsForValue().get(id);
            if (transfer == null)
                return result.withFailure(ExceptionType.NOT_FOUND);

            // Status
            StatusType status = application.getStatus();
            if (StatusType.NOT_PASSED.equals(status))
                return result.withFailure("已拒绝的申请不能接受");
            else if (StatusType.PASSED.equals(status))
                return result;

            application.setStatus(StatusType.PASSED);
            elasticTemplate.save(application);
            reviewService.transferPatent(transfer.getPatentId(), transfer);
            messageService.sendAcceptMessage(application);
        }
        return result;
    }

}
