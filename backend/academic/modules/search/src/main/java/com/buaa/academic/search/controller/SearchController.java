package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.entity.item.InstitutionItem;
import com.buaa.academic.document.entity.item.JournalItem;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.document.entity.item.ResearcherItem;
import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.dao.InstitutionRepository;
import com.buaa.academic.search.dao.JournalRepository;
import com.buaa.academic.search.dao.ResearcherRepository;
import com.buaa.academic.search.model.request.Condition;
import com.buaa.academic.search.model.request.Filter;
import com.buaa.academic.search.model.request.SearchRequest;
import com.buaa.academic.search.model.request.SmartSearchRequest;
import com.buaa.academic.search.model.response.HitPage;
import com.buaa.academic.search.model.response.SmartPage;
import com.buaa.academic.search.service.SearchService;
import com.buaa.academic.search.util.HitsReducer;
import com.buaa.academic.tool.translator.Translator;
import com.buaa.academic.tool.util.StringUtils;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${search.conditions.max-depth}")
    private int maxDepth;

    @Value("${spring.elasticsearch.highlight.pre-tag}")
    private String preTag;

    @Value("${spring.elasticsearch.highlight.post-tag}")
    private String postTag;

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
        String keyword = StringUtils.strip(searchRequest.getKeyword(), 64);

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
        } else {
            keywords = new String[]{keyword};
        }

        // Filters
        QueryBuilder filter = searchService.buildFilters(filters);

        // Sort
        String srt = searchRequest.getSort();
        SortBuilder<?> sort = searchService.buildSort(srt);

        // Page
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Paper> baseHits = searchService.smartSearch(keywords, filter, sort, page);

        // Set items & statistics
        smartPage.setHits(baseHits);
        smartPage.setStatistics(baseHits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

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
    @ApiOperation(
            value = "学术论文检索",
            notes = "使用可复合的条件搜索学术论文。</br>" +
                    "<b>本接口请求可以允许多层嵌套子条件</b></br>" +
                    "返回值包括论文检索结果。</br>" +
                    "允许添加Filter的字段：year (below, above, range, equal), citationNum (above)")
    public Result<HitPage<PaperItem>> searchPapers(@RequestBody @Valid SearchRequest searchRequest) throws AcademicException {
        long start = System.currentTimeMillis();
        Result<HitPage<PaperItem>> result = new Result<>();
        HitPage<PaperItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();
        List<Filter> filters = searchRequest.getFilters();
        String srt = searchRequest.getSort(); // Sort

        /* TODO: 2021/11/18 进行参数检查
            (1) 若条件树的层数超过 maxDepth 则 return result.withFailure(<合适的错误信息>)
            (2) 遍历条件树，对所有叶节点的 keyword 进行 strip 和截断，可以参考上一个方法 (修改代码风格和考虑String = null的情况）
            (3) 遍历过程中，对所有叶节点基于 scope 进行检查和处理。学术论文所允许限定的 scope 规则为：
                "title", "abstract", "keywords", "subjects", "topics", "institutions.name": 允许开启 fuzzy 和 translated
                "journal.title": 允许开启 fuzzy，但不允许开启 translated
                "type", "authors.name": 不允许开启 fuzzy 和 translated
                要求可读性和可扩展性，以便后续添加新的 scope 规则.
                不符合规则时，可以 return result.withFailure(ExceptionType.INVALID_PARAM), 也可以将对应字段强制 set 回 false
            (4) 顺序遍历所有过滤器，对 type 和 attr 进行检查。学术论文所允许添加的过滤器规则可以参考上一个方法.
            (5) 对排序字段进行检查。学术论文允许的排序规则为 null, "date.asc", "date.desc", "citationNum.desc".
                若不符合，return result.withFailure(ExceptionType.INVALID_PARAM)
            (6) 涉及某字段允许多个值的参数检查 禁止使用 if (xxx || xxx || ... || xxx) 这种结构.
         */
        // Condition tree depth check
        countDepth(conditions, maxDepth);

        // Keyword strip
        stripKeyword(conditions);

        // Scope check
        checkScope(conditions);

        // Param check for filters
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

        // Sort check
        if(!srt.equals("")){
            switch(srt){
                case "date.asc":
                case "date.desc":
                case "citationNum.desc":
                    break;
                default: return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }

        // Multiple params check

        // Conditions
        QueryBuilder query;
        if (conditions.size() == 1) {
            query = conditions.get(0).compile();
        } else {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (Condition condition : conditions) {
                switch (condition.getLogic()) {
                    case "and" -> boolQuery.must(condition.compile());
                    case "or" -> boolQuery.should(condition.compile());
                    case "not" -> boolQuery.mustNot(condition.compile());
                }
            }
            query = boolQuery;
        }

        // Filters
        QueryBuilder filter = searchService.buildFilters(filters);

        // Sort
        SortBuilder<?> sort = searchService.buildSort(srt);

        // Highlight
        HighlightBuilder hlt = searchService.buildHighlight("title", "abstract", "keywords");

        // Page
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Paper> hits = searchService.advancedSearch(Paper.class, query, filter, sort, hlt, page);

        // Set items & statistics
        hitPage.setItems(HitsReducer.reducePaperHits(hits, preTag, postTag));
        hitPage.setStatistics(hits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        // Set time cost
        hitPage.setTimeCost(System.currentTimeMillis() - start);

        return result.withData(hitPage);
    }

    private void stripKeyword(List<Condition> conditions) {
        for (Condition condition : conditions) {
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 64));
            if (condition.isCompound()) {
                stripKeyword(condition.getSubConditions());
            }
        }
    }

    private void checkScope(List<Condition> conditions) throws AcademicException {
        for (Condition condition : conditions) {
            if (condition.isCompound()) {
                checkScope(condition.getSubConditions());
            }
            if (condition.getScope() != null) {
                for (String s : condition.getScope()) {
                    if (!s.equals("")) {
                        switch (s) {
                            case "journal.title":
                            case "type":
                            case "authors.name":
                                break;
                            default:
                                throw new AcademicException("条件冲突");
                        }
                    }
                }
            }
        }
    }

    private void countDepth(List<Condition> conditions, int maxDepth) throws AcademicException {
        for (Condition condition : conditions) {
            if (maxDepth < 0) {
                throw new AcademicException("条件复合层数过高");
            }
            if (condition.isCompound()) {
                countDepth(condition.getSubConditions(), maxDepth - 1);
            }
        }
    }
}
