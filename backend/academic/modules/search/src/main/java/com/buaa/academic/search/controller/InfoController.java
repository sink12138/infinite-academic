package com.buaa.academic.search.controller;

import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.RequestModel;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "实体信息获取")
public class InfoController {

    @PostMapping("/test")
    @ApiOperation(value = "测试接口", notes = "返回传入的相同字符串")
    public Result<String> test(@RequestBody RequestModel<String> model) throws AcademicException {
        Result<String> result = new Result<>();
        // Param checks
        if (model == null)
            return result.withFailure(ExceptionType.ILLEGAL_FORMAT);
        String param = model.getData();
        if (param == null)
            throw new AcademicException(ExceptionType.ILLEGAL_FORMAT);

        if (param.equals("Throw an exception")) {
            throw new AcademicException(ExceptionType.INTERNAL_SERVER_ERROR);
        }
        else if (param.equals("Return a failure")) {
            return result.withFailure(ExceptionType.INTERNAL_SERVER_ERROR);
        }
        return result.withData(param);
    }

}
