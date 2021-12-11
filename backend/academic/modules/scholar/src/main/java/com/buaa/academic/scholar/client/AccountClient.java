package com.buaa.academic.scholar.client;

import com.buaa.academic.model.web.Result;
import com.buaa.academic.tool.validator.AllowValues;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@FeignClient("client-account")
public interface AccountClient {

    @PostMapping("/sendVerifyCode")
    Result<Void> sendVerifyCode(@RequestHeader(value = "Auth", required = false) String userId,
                                @RequestParam(value = "email") @Email String email,
                                @RequestParam(value = "action") @AllowValues({"forgetPwd", "researcherCtf"})  @NotNull String action);
}
