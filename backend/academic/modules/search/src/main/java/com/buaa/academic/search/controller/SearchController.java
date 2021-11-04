package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.item.PaperHit;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.model.HitPage;
import com.buaa.academic.search.model.SearchRequest;
import com.buaa.academic.search.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@Api(tags = "实体批量检索")
public class SearchController {

    @PostMapping("/")
    @ApiOperation(
            value = "检索总入口",
            notes = "所有实体的检索总入口（包括简单搜索、复合搜索和树形搜索）。</br>" +
                    "可以直接将请求发送到各实体类的查询接口，或将需要检索的实体类别放在请求头中，由本接口进行转发。</br>" +
                    "<b>本接口需要请求体，也定义了响应体</b>，具体结构说明见对应实体类的查询接口。")
    @ApiImplicitParam(name = "Target", value = "需要查询的实体类别：paper/researcher/journal/institution/patent")
    public void search(@RequestHeader("Target") @Valid @AllowValues({"lite", "paper", "researcher", "journal", "institution", "patent"}) String target,
                       HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/" + target).forward(request, response);
    }

    @PostMapping("/lite")
    @ApiOperation(value = "轻量搜索", notes = "使用一个字符串进行简易搜索，自动推测目标实体类别")
    public Result<List<Map<String, String>>> liteSearch(@RequestParam("word") @Valid @NotBlank String word) {

        return new Result<>();
    }

    @PostMapping("/paper")
    @ApiOperation(value = "学术论文检索", notes = "学术论文（Paper）类检索的专用接口")
    public Result<HitPage<PaperHit, Paper>> searchPapers(@RequestBody @Valid SearchRequest searchRequest) {
        return new Result<>();
    }

}
