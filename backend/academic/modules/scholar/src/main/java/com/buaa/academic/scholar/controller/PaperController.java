package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.entity.User;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.*;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.client.ResourceClient;
import com.buaa.academic.scholar.client.SpiderClient;
import com.buaa.academic.scholar.model.AutoPaper;
import com.buaa.academic.scholar.service.ApplicationService;
import com.buaa.academic.scholar.utils.ExistenceCheck;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/paper")
@Validated
@Api(tags = "提交论文相关申请")
public class PaperController {

    @Autowired
    private ApplicationService<PaperRemove> paperRemoveAppService;

    @Autowired
    private ApplicationService<PaperAdd> paperAddService;

    @Autowired
    private ExistenceCheck existenceCheck;

    @Autowired
    private ResourceClient resourceClient;

    @Autowired
    private SpiderClient spiderClient;

    @Autowired
    private ApplicationService<PaperEdit> paperEditService;

    @Autowired
    private ElasticsearchRestTemplate template;

    @PostMapping("/auto")
    @ApiOperation(
            value = "自助添加论文",
            notes = "提交URL或文章标题，由系统自动添加论文。URL和title必须有且仅有一个不为null。当前支持的网站及URL格式：</br>" +
                    "<b>万方数据</b>：https://d.wanfangdata.com.cn/periodical/... 或 https://d.wanfangdata.com.cn/thesis/...</br>" +
                    "<b>中国知网</b>：https://kns.cnki.net/kcms/detail/detail.aspx...")
    public Result<Void> auto(@RequestHeader(value = "Auth") String userId,
                             @RequestBody @Valid AutoPaper autoPaper) {
        Result<Void> result = new Result<>();
        String url = autoPaper.getUrl();
        String title = autoPaper.getTitle();
        if (url == null && title == null)
            return result.withFailure(ExceptionType.INVALID_PARAM);
        else if (url != null) {
            if (!url.matches("^(https://)?d\\.wanfangdata\\.com\\.cn/(periodical|thesis)/[0-9A-Za-z%_-]+$") &&
                    !url.matches("^(https://)?kns\\.cnki\\.net/kcms/detail/detail\\.aspx\\?[A-Za-z]+=[0-9A-Za-z-_]+(&[A-Za-z]+=[0-9A-Za-z-_]+)*$"))
                return result.withFailure("不支持的URL格式");
            return spiderClient.url(userId, url);
        }
        else {
            if (title.isBlank())
                return result.withFailure(ExceptionType.INVALID_PARAM);
            return spiderClient.title(userId, title);
        }
    }

    @ApiOperation(value = "添加论文", notes = "添加已发表的文章或在本网站首发文章")
    @PostMapping("/add")
    public Result<Void> addPaper(@RequestHeader(value = "Auth") String userId,
                                 @RequestBody @Valid ApplicationInfo<PaperAdd> paper) {
        Result<Void> result = new Result<>();
        if (!existenceCheck.paperAddCheck(paper.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (paper.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(paper.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!writtenBySelf(paper.getContent().getAdd(), userId))
            return result.withFailure("不能提交自己未参与创作的论文");
        if (!paperAddService.submitAppWithCtf(paper, userId, ApplicationType.NEW_PAPER)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        return result;
    }

    @ApiOperation(value = "修改论文", notes = "修改论文")
    @PostMapping("/edit")
    public Result<Void> editPaper(@RequestHeader(value = "Auth") String userId,
                                  @RequestBody @Valid ApplicationInfo<PaperEdit> paper) {
        Result<Void> result = new Result<>();
        if (paper.getFileToken() != null) {
            Result<Boolean> fileExistRes = resourceClient.exists(paper.getFileToken());
            if (!fileExistRes.getData())
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        if (!existenceCheck.paperEditCheck(paper.getContent()))
            return result.withFailure(ExceptionType.NOT_FOUND);
        if (!writtenBySelf(paper.getContent().getEdit(), userId))
            return result.withFailure("不能提交自己未参与创作的论文");
        if (!paperEditService.submitAppWithCtf(paper, userId, ApplicationType.EDIT_PAPER)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }
        return new Result<>();
    }

    @PostMapping("/remove")
    @ApiOperation(value = "移除论文")
    public Result<Void> removePaper(@RequestHeader(value = "Auth") String userId,
                                    @RequestBody @Valid ApplicationInfo<PaperRemove> paper) {
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean writtenBySelf(PaperForApp paper, String userId) {
        User user = Objects.requireNonNull(template.get(userId, User.class));
        String researcherId = Objects.requireNonNull(user.getResearcherId());
        for (PaperForApp.Author author : paper.getAuthors()) {
            if (researcherId.equals(author.getId()))
                return true;
        }
        return false;
    }

}
