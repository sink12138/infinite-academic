package com.buaa.academic.account.controller;

import com.buaa.academic.account.client.ResourceClient;
import com.buaa.academic.account.utils.ExistenceCheck;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.model.application.Transfer;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

@RestController
@Validated
@Api(tags = "专利转让相关")
public class TransferController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private ResourceClient resourceClient;

    @Autowired
    private ExistenceCheck existenceCheck;

    @PostMapping("/patent/transfer")
    @ApiOperation(value = "专利转让申请提交")
    public Result<Void> transferSubmit(@RequestHeader(value = "Auth") String userId,
                                       @RequestBody @Valid ApplicationInfo<Transfer> transferInfo) {
        Result<Void> result = new Result<>();
        if (transferInfo.getFileToken() == null && transferInfo.getWebsiteLink() == null)
            return result.withFailure(ExceptionType.INVALID_PARAM);
        // File token existence check
        String fileToken = transferInfo.getFileToken();
        if (fileToken != null && !resourceClient.exists(fileToken).getData())
            return result.withFailure(ExceptionType.INVALID_PARAM);

        if (!existenceCheck.transferCheck(transferInfo.getContent()))
            return result.withFailure(ExceptionType.INVALID_PARAM);

        Application application = new Application(
                null,
                userId,
                transferInfo.getEmail(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),
                ApplicationType.TRANSFER,
                StatusType.UNDER_REVIEW,
                fileToken,
                transferInfo.getWebsiteLink());
        template.save(application);
        redisTemplate.opsForValue().set(application.getId(), transferInfo.getContent());
        redisTemplate.expire(application.getId(), Duration.ofDays(30));
        return result;
    }
}
