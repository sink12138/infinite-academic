package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.RequestModel;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController("/info")
@Validated
@Api(tags = "实体详细信息")
public class InfoController {

    @PostMapping("/test")
    @ApiOperation(value = "测试接口", notes = "返回传入的相同字符串")
    public Result<String> test(@RequestBody @Valid RequestModel<String> model) throws AcademicException {
        Result<String> result = new Result<>();
        String param = model.getData();
        if (param.equals("Throw an exception")) {
            throw new AcademicException(ExceptionType.INTERNAL_SERVER_ERROR);
        }
        else if (param.equals("Return a failure")) {
            return result.withFailure(ExceptionType.INTERNAL_SERVER_ERROR);
        }
        return result.withData(param);
    }

    @GetMapping("/test/user")
    @ApiOperation(value = "测试用户实体", notes = "测试用户实体的信息显示效果")
    public Result<User> testUser() {
        return new Result<>();
    }

    @GetMapping("/test/paper")
    @ApiOperation(value = "测试学术论文实体", notes = "测试用户实体的信息显示效果")
    public Result<Paper> testPaper() {
        return new Result<>();
    }

    @GetMapping("/test/researcher")
    @ApiOperation(value = "测试科研人员实体", notes = "测试用户实体的信息显示效果")
    public Result<Researcher> testResearcher() {
        return new Result<>();
    }

    @GetMapping("/test/institution")
    @ApiOperation(value = "测试科研机构实体", notes = "测试用户实体的信息显示效果")
    public Result<Institution> testInstitution() {
        return new Result<>();
    }

    @GetMapping("/test/journal")
    @ApiOperation(value = "测试期刊实体", notes = "测试用户实体的信息显示效果")
    public Result<Journal> testJournal() {
        return new Result<>();
    }

    @GetMapping("/test/patent")
    @ApiOperation(value = "测试专利实体", notes = "测试用户实体的信息显示效果")
    public Result<Patent> testPatent() {
        return new Result<>();
    }

}
