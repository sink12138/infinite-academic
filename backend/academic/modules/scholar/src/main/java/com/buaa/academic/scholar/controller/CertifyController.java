package com.buaa.academic.scholar.controller;

import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/certify")
@Validated
@Api(tags = "学者认证相关")
public class CertifyController {

    @PostMapping("/")
    @ApiIgnore
    public Result<Void> certify() {
        return new Result<>();
    }

}
