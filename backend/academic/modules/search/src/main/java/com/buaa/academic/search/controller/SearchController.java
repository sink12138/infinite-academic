package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.entity.item.InstitutionItem;
import com.buaa.academic.document.entity.item.JournalItem;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.document.entity.item.ResearcherItem;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.dao.InstitutionRepository;
import com.buaa.academic.search.dao.JournalRepository;
import com.buaa.academic.search.dao.ResearcherRepository;
import com.buaa.academic.search.model.request.Filter;
import com.buaa.academic.search.model.request.SearchRequest;
import com.buaa.academic.search.model.request.SmartSearchRequest;
import com.buaa.academic.search.model.response.HitPage;
import com.buaa.academic.search.model.response.SmartPage;
import com.buaa.academic.search.service.SearchService;
import com.buaa.academic.tool.translator.Translator;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Validated
@Api(tags = "实体批量检索")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private ResearcherRepository researcherRepository;

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

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
                    "返回值包括论文检索结果和可能的推荐信息。</br>" +
                    "允许添加Filter的字段：year (below, above, range, equal), citationNum (above)")
    public Result<SmartPage> smartSearch(@RequestBody @Valid SmartSearchRequest searchRequest) {
        long start = System.currentTimeMillis();
        Result<SmartPage> result = new Result<>();
        SmartPage smartPage = new SmartPage();

        // Strip keyword
        String keyword = searchRequest.getKeyword().trim().replaceAll("\\s+", " ");
        if (keyword.length() > 32)
            keyword = keyword.substring(0, 32);

        // Param check for filters
        List<Filter> filters = searchRequest.getFilters();
        for (Filter filter : filters) {
            String type = filter.getType();
            switch (type) {
                case "below", "range", "equal" -> {
                    if (!filter.getAttr().equals("year"))
                        return result.withFailure(ExceptionType.INVALID_PARAM);
                }
                case "above" -> {
                    if (!filter.getAttr().matches("^(year)|(citationNum)$"))
                        return result.withFailure(ExceptionType.INVALID_PARAM);
                }
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Search for base items (papers)
        // Keywords
        String[] keywords;
        if (searchRequest.isTranslated()) {
            Set<String> keySet = new HashSet<>(6);
            keySet.add(keyword);
            keySet.add(Translator.translate(keyword, "auto", "zh"));
            keySet.add(Translator.translate(keyword, "auto", "en"));
            keywords = keySet.toArray(new String[0]);
        }
        else {
            keywords = new String[] { keyword };
        }

        // Filters
        BoolQueryBuilder filter = null;
        if (!filters.isEmpty()) {
            filter = QueryBuilders.boolQuery();
            for (Filter flt : filters) {
                filter.must(flt.compile());
            }
        }

        // Sort
        String srt = searchRequest.getSort();
        SortBuilder<?> sort;
        if (srt == null)
            sort = SortBuilders.scoreSort();
        else {
            String[] srtParams = srt.split("\\.");
            sort = SortBuilders.fieldSort(srtParams[0]);
            if (srtParams[1].equals("asc"))
                sort.order(SortOrder.ASC);
            else
                sort.order(SortOrder.DESC);
        }

        // Page
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Paper> baseHits = searchService.smartSearch(keywords, filter, sort, page);

        // Set items & statistics
        smartPage.setHits(baseHits);
        smartPage.setTotalHits(baseHits.getTotalHits());
        int pageSize = searchRequest.getSize();
        long totalHits = baseHits.getTotalHits();
        int totalPages = (int) ((totalHits + pageSize - 1) / pageSize);
        smartPage.setTotalPages(totalPages);
        smartPage.setSize(baseHits.getSearchHits().size());
        int pageNum = searchRequest.getPage();
        smartPage.setPage(Math.min(pageNum, totalPages - 1));

        // Detect recommendations
        if (searchRequest.getPage() == 0) {
            // Search for researchers
            SearchPage<Researcher> researchersByName = researcherRepository.findByNameEquals(keyword, PageRequest.of(0, 6));
            if (!researchersByName.isEmpty()) {
                smartPage.setDetection("researcher");
                List<ResearcherItem> researchers = new ArrayList<>();
                researchersByName.forEach(item -> researchers.add(item.getContent().reduce()));
                smartPage.setRecommendation(researchers);
                smartPage.setTimeCost(System.currentTimeMillis() - start);
                return result.withData(smartPage);
            }

            float baseScore = baseHits.getMaxScore();

            // Search for journals
            SearchPage<Journal> journalsByTitle = journalRepository.findByTitleLike(keyword, PageRequest.of(0, 6));
            if (journalsByTitle.getSearchHits().getMaxScore() >= baseScore * 0.75) {
                smartPage.setDetection("journal");
                List<JournalItem> journals = new ArrayList<>();
                journalsByTitle.forEach(item -> journals.add(item.getContent().reduce()));
                smartPage.setRecommendation(journals);
                smartPage.setTimeCost(System.currentTimeMillis() - start);
                return result.withData(smartPage);
            }

            // Search for institutions
            SearchPage<Institution> institutionsByName = institutionRepository.findByNameLike(keyword, PageRequest.of(0, 6));
            if (institutionsByName.getSearchHits().getMaxScore() >= baseScore * 0.75) {
                smartPage.setDetection("institution");
                List<InstitutionItem> institutions = new ArrayList<>();
                institutionsByName.forEach(item -> institutions.add(item.getContent().reduce()));
                smartPage.setRecommendation(institutions);
                smartPage.setTimeCost(System.currentTimeMillis() - start);
                return result.withData(smartPage);
            }
        }
        smartPage.setTimeCost(System.currentTimeMillis() - start);
        return result.withData(smartPage);
    }

    @PostMapping("/paper")
    @ApiOperation(value = "学术论文检索", notes = "学术论文（Paper）类检索的专用接口")
    public Result<HitPage<PaperItem>> searchPapers(@RequestBody @Valid SearchRequest searchRequest) {
        return new Result<>();
    }

}
