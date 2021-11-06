package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.document.entity.item.ResearcherItem;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.dao.ResearcherRepository;
import com.buaa.academic.search.model.request.SearchRequest;
import com.buaa.academic.search.model.request.SmartSearchRequest;
import com.buaa.academic.search.model.response.HitPage;
import com.buaa.academic.search.model.response.SmartPage;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
@Api(tags = "实体批量检索")
public class SearchController {

    @Autowired
    ResearcherRepository researcherRepository;

    @PostMapping("/")
    @ApiOperation(
            value = "检索总入口",
            notes = "所有实体的检索总入口（包括简单搜索、复合搜索和树形搜索）。</br>" +
                    "可以直接将请求发送到各实体类的查询接口，或将需要检索的实体类别放在请求头中，由本接口进行转发。</br>" +
                    "<b>本接口需要请求体，也定义了响应体</b>，具体结构说明见对应实体类的查询接口。")
    @ApiImplicitParam(name = "Target", value = "需要查询的实体类别：paper/researcher/journal/institution/patent")
    public void search(@RequestHeader("Target") @Valid @AllowValues({"smart", "paper", "researcher", "journal", "institution", "patent"}) String target,
                       HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/" + target).forward(request, response);
    }

    @PostMapping("/smart")
    @ApiOperation(
            value = "智能搜索",
            notes = "使用一条关键词搜索学术论文。</br>" +
                    "如果在其他实体上出现了精准的命中（例如学者、机构、期刊的名字），会将推荐信息一并返回。</br>" +
                    "返回值包括论文检索结果和可能的推荐信息。")
    public Result<SmartPage> smartSearch(@RequestBody @Valid SmartSearchRequest searchRequest) {
        SmartPage smartPage = new SmartPage();
        String keyword = searchRequest.getKeyword().trim().replaceAll("\\s+", " ");
        if (keyword.length() > 32)
            keyword = keyword.substring(0, 32);
        // TODO: 2021/11/6 smartPage.withHits()
        SearchPage<Researcher> researchersByName = researcherRepository.findByNameEquals(keyword, PageRequest.of(0, 6));
        if (!researchersByName.isEmpty()) {
            smartPage.setDetection("researcher");
            List<ResearcherItem> researchers = new ArrayList<>();
            researchersByName.forEach(item -> researchers.add(item.getContent().reduce()));
            smartPage.setRecommendation(researchers);
        }
        return new Result<>();
    }

    @PostMapping("/paper")
    @ApiOperation(value = "学术论文检索", notes = "学术论文（Paper）类检索的专用接口")
    public Result<HitPage<PaperItem>> searchPapers(@RequestBody @Valid SearchRequest searchRequest) {
        return new Result<>();
    }

}
