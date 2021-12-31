package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.model.request.BriefRequest;
import com.buaa.academic.search.service.InfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/info")
@Validated
@Api(tags = "实体详细信息")
public class InfoController {

    @Autowired
    private InfoService infoService;

    @Autowired
    private ElasticsearchRestTemplate template;

    @GetMapping("/paper/{id}")
    @ApiOperation(value = "学术论文详细信息", notes = "根据论文编号显示文章详细信息")
    @ApiImplicitParam(name = "id", value = "论文ID")
    public Result<Paper> paperInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Paper> result = new Result<>();
        Paper paper = infoService.findDocument(Paper.class, id);
        if (paper == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(paper);
    }

    @GetMapping("/researcher/{id}")
    @ApiOperation(value = "科研人员详细信息", notes = "根据科研人员编号显示科研人员详细信息")
    @ApiImplicitParam(name = "id", value = "科研人员ID")
    public Result<Researcher> researcherInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Researcher> result = new Result<>();
        Researcher researcher = infoService.findDocument(Researcher.class, id);
        if (researcher == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(researcher);
    }

    @GetMapping("/journal/{id}")
    @ApiOperation(value = "学术期刊详细信息", notes = "根期刊编号显示期刊详细信息")
    @ApiImplicitParam(name = "id", value = "期刊ID")
    public Result<Journal> journalInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Journal> result = new Result<>();
        Journal journal = infoService.findDocument(Journal.class, id);
        if (journal == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(journal);
    }

    @GetMapping("/institution/{id}")
    @ApiOperation(value = "学术机构详细信息", notes = "根据机构编号显示机构详细信息")
    @ApiImplicitParam(name = "id", value = "机构ID")
    public Result<Institution> institutionInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Institution> result = new Result<>();
        Institution institution = infoService.findDocument(Institution.class, id);
        if (institution == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(institution);
    }

    @GetMapping("/patent/{id}")
    @ApiOperation(value = "专利详细信息", notes = "根据专利编号显示专利详细信息")
    @ApiImplicitParam(name = "id", value = "专利ID")
    public Result<Patent> patentInfo(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id) {
        Result<Patent> result = new Result<>();
        Patent patent = infoService.findDocument(Patent.class, id);
        if (patent == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(patent);
    }

    @PostMapping("/brief")
    @ApiOperation(value = "查询简要信息", notes = "返回体可以参考搜索结果接口")
    public Result<List<Object>> brief(@RequestBody @Valid BriefRequest request) {
        Result<List<Object>> result = new Result<>();
        List<Object> items = new ArrayList<>();
        switch (request.getEntity()) {
            case "paper" -> {
                for (String id : request.getIds()) {
                    Paper paper = template.get(id, Paper.class);
                    if (paper == null)
                        return result.withFailure(ExceptionType.NOT_FOUND);
                    items.add(paper.reduce());
                }
            }
            case "researcher" -> {
                for (String id : request.getIds()) {
                    Researcher researcher = template.get(id, Researcher.class);
                    if (researcher == null)
                        return result.withFailure(ExceptionType.NOT_FOUND);
                    items.add(researcher.reduce());
                }
            }
            case "journal" -> {
                for (String id : request.getIds()) {
                    Journal journal = template.get(id, Journal.class);
                    if (journal == null)
                        return result.withFailure(ExceptionType.NOT_FOUND);
                    items.add(journal.reduce());
                }
            }
            case "institution" -> {
                for (String id : request.getIds()) {
                    Institution institution = template.get(id, Institution.class);
                    if (institution == null)
                        return result.withFailure(ExceptionType.NOT_FOUND);
                    items.add(institution.reduce());
                }
            }
            case "patent" -> {
                for (String id : request.getIds()) {
                    Patent patent = template.get(id, Patent.class);
                    if (patent == null)
                        return result.withFailure(ExceptionType.NOT_FOUND);
                    items.add(patent.reduce());
                }
            }
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        return result.withData(items);
    }

}
