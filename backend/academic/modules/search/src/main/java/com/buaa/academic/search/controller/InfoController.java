package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.RequestModel;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController("/info")
@Validated
@Api(tags = "实体详细信息")
public class InfoController {

    @Autowired
    private SearchService searchService;

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

    @GetMapping("/paper/{id}")
    @ApiOperation(value = "学术论文详细信息", notes = "根据论文编号显示文章详细信息")
    @ApiImplicitParam(name = "id", value = "论文ID")
    public Result<Paper> paperInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Paper> result = new Result<>();
        Paper paper = searchService.searchById(Paper.class, id);
        if (paper == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(paper);
    }

    @GetMapping("/researcher/{id}")
    @ApiOperation(value = "科研人员详细信息", notes = "根据科研人员编号显示科研人员详细信息")
    @ApiImplicitParam(name = "id", value = "科研人员ID")
    public Result<Researcher> researcherInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Researcher> result = new Result<>();
        Researcher researcher = searchService.searchById(Researcher.class, id);
        if (researcher == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(researcher);
    }

    @GetMapping("/journal/{id}")
    @ApiOperation(value = "学术期刊详细信息", notes = "根期刊编号显示期刊详细信息")
    @ApiImplicitParam(name = "id", value = "期刊ID")
    public Result<Journal> journalInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Journal> result = new Result<>();
        Journal journal = searchService.searchById(Journal.class, id);
        if (journal == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(journal);
    }

    @GetMapping("/institution/{id}")
    @ApiOperation(value = "学术机构详细信息", notes = "根据机构编号显示机构详细信息")
    @ApiImplicitParam(name = "id", value = "机构ID")
    public Result<Institution> institutionInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Institution> result = new Result<>();
        Institution institution = searchService.searchById(Institution.class, id);
        if (institution == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(institution);
    }

    @GetMapping("/patent/{id}")
    @ApiOperation(value = "学术机构详细信息", notes = "根据机构编号显示机构详细信息")
    @ApiImplicitParam(name = "id", value = "机构ID")
    public Result<Patent> patentInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Patent> result = new Result<>();
        Patent patent = searchService.searchById(Patent.class, id);
        if (patent == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(patent);
    }

}
