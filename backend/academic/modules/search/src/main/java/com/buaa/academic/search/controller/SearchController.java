package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.document.entity.item.*;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.config.HighlightConfiguration;
import com.buaa.academic.search.dao.InstitutionRepository;
import com.buaa.academic.search.dao.JournalRepository;
import com.buaa.academic.search.dao.ResearcherRepository;
import com.buaa.academic.search.model.request.Condition;
import com.buaa.academic.search.model.request.Filter;
import com.buaa.academic.search.model.request.Filter.FilterFormat;
import com.buaa.academic.search.model.request.Filter.FilterType;
import com.buaa.academic.search.model.request.SearchRequest;
import com.buaa.academic.search.model.request.SmartSearchRequest;
import com.buaa.academic.search.model.response.HitPage;
import com.buaa.academic.search.model.response.SmartPage;
import com.buaa.academic.search.service.SearchService;
import com.buaa.academic.search.service.SuggestService;
import com.buaa.academic.search.util.HitsReducer;
import com.buaa.academic.tool.util.StringUtils;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@Api(tags = "实体批量检索")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private SuggestService suggestService;

    @Autowired
    private ResearcherRepository researcherRepository;

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private HighlightConfiguration hltConfig;

    @Value("${search.conditions.max-depth}")
    private int maxDepth;

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
                    "允许添加的filter和sort规则与学术论文检索接口相同。")
    public Result<SmartPage> smartSearch(@RequestBody @Valid SmartSearchRequest searchRequest,
                                         HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<SmartPage> result = new Result<>();
        SmartPage smartPage = new SmartPage();

        List<Filter> filters = searchRequest.getFilters();
        String srt = searchRequest.getSort();

        // Param check for filters
        if (!validatePaperFilters(filters)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }

        // Param check for sort
        if (srt != null) {
            switch (srt) {
                case "date.asc", "date.desc", "citationNum.desc" -> {}
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Strip keyword
        String keyword = StringUtils.strip(searchRequest.getKeyword(), 128);

        // Base highlight fields
        List<String> highlightFields = new ArrayList<>() {{
            add("title");
            add("keywords");
            add("abstract");
        }};

        QueryBuilder detectionQuery = null;

        // Detect recommendations or read from cache
        QueryBuilder researcherDetection = QueryBuilders.termQuery("authors.name", keyword).boost(5.0f);
        QueryBuilder institutionDetection = QueryBuilders.matchQuery("institutions.name", keyword).boost(5.0f);
        QueryBuilder journalDetection = QueryBuilders.matchQuery("journal.title", keyword).boost(5.0f);
        HttpSession session = httpServletRequest.getSession();
        List<String> completion = suggestService.completionSuggest(Paper.class, keyword, "completion", 2);
        String detection = null;
        if (searchRequest.getPage() == 0) {
            // noinspection ConstantConditions
            do {
                // Search for researchers
                SearchPage<Researcher> researchersByName = researcherRepository.findByNameEquals(keyword, PageRequest.of(0, 6, Sort.by(Sort.Order.desc("citationNum"))));
                if (!researchersByName.isEmpty()) {
                    detection = "researcher";
                    List<ResearcherItem> researchers = new ArrayList<>();
                    researchersByName.forEach(item -> researchers.add(item.getContent().reduce()));
                    smartPage.setRecommendation(researchers);
                    break;
                }

                // Search for institutions
                if (keyword.length() < 4 || keyword.matches("^[A-Z][a-z\\s]{3}.*$") && keyword.length() < 12)
                    break;
                SearchPage<Institution> institutionsByName = institutionRepository.findByNameMatches(keyword, PageRequest.of(0, 6));
                List<InstitutionItem> institutions = new ArrayList<>();
                for (SearchHit<Institution> hit : institutionsByName) {
                    Institution institution = hit.getContent();
                    if (institution.getName().startsWith(keyword))
                        institutions.add(institution.reduce());
                }
                if (!institutions.isEmpty()) {
                    detection = "institution";
                    smartPage.setRecommendation(institutions);
                    break;
                }

                // Search for journals
                if (completion.size() == 2)
                    break;
                SearchPage<Journal> journalsByTitle = journalRepository.findByTitleMatches(keyword, PageRequest.of(0, 6));
                List<JournalItem> journals = new ArrayList<>();
                for (SearchHit<Journal> hit : journalsByTitle) {
                    Journal journal = hit.getContent();
                    if (journal.getTitle().startsWith(keyword))
                        journals.add(journal.reduce());
                }
                if (!journals.isEmpty()) {
                    detection = "journal";
                    smartPage.setRecommendation(journals);
                    break;
                }
            } while (false);
            if (detection != null) {
                smartPage.setDetection(detection);
                session.setAttribute("detection", detection);
            }
        }
        else {
            detection = (String) session.getAttribute("detection");
        }

        if (detection != null) {
            switch (detection) {
                case "researcher" -> {
                    highlightFields.add("authors.name");
                    searchRequest.setTranslated(false);
                    detectionQuery = researcherDetection;
                }
                case "institution" -> {
                    highlightFields.add("institutions.name");
                    detectionQuery = institutionDetection;
                }
                case "journal" -> {
                    highlightFields.add("journal.title");
                    detectionQuery = journalDetection;
                }
            }
        }

        // Corrections
        String correctKeyword = keyword;
        if (detectionQuery == null && completion.isEmpty()) {
            List<String> corrections = suggestService.correctionSuggest(Paper.class, keyword,
                    new String[] { "title.phrase", "keywords.phrase", "subjects.phrase",
                            "title.raw", "keywords.raw", "subjects.raw" }, 1);
            if (!corrections.isEmpty()) {
                correctKeyword = corrections.get(0).replaceAll(hltConfig.preTag(), "").replaceAll(hltConfig.postTag(), "");
                smartPage.setCorrection(correctKeyword);
            }
        }

        // Build conditions
        List<Condition> conditions = List.of(
                new Condition(
                        "or",
                        false,
                        correctKeyword,
                        Set.of("title", "keywords", "abstract"),
                        true,
                        searchRequest.isTranslated(),
                        searchRequest.isTranslated() ? Set.of("zh", "en") : null,
                        null));

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "paper");
        if (detectionQuery != null) {
            query = QueryBuilders.boolQuery().should(query).should(detectionQuery);
        }
        QueryBuilder filter = searchService.buildFilter(filters, "paper");
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight(highlightFields.toArray(new String[0]));
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Store to cache
        session.setAttribute("index", "paper");
        session.setAttribute("query", query.toString());
        if (filter != null)
            session.setAttribute("filter", filter.toString());

        // Run search
        SearchHits<Paper> baseHits = searchService.runSearch(Paper.class, query, filter, sort, hlt, page);

        // Set items & statistics
        smartPage.setHits(baseHits, hltConfig.preTag(), hltConfig.postTag());
        smartPage.setStatistics(baseHits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        smartPage.setTimeCost(System.currentTimeMillis() - start);
        return result.withData(smartPage);
    }

    @PostMapping("/paper")
    @ApiOperation(
            value = "学术论文检索",
            notes = """
                    使用多个条件或嵌套条件搜索学术论文。
                    
                    条件中的scope参数允许的所有值的含义与限制规则如下：</br>
                    <b>title</b> - 论文标题</br>
                    <b>abstract</b> - 论文摘要</br>
                    <b>keywords</b> - 论文关键词</br>
                    <b>subjects</b> - 论文学科分类</br>
                    <b>type</b> - 论文本身的类别，如学位论文、期刊论文等，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    <b>authors.name</b> - 论文作者的姓名，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    <b>institutions.name</b> - 发表论文的机构名，不允许与其他scope共存</br>
                    <b>journal.title</b> - 刊登论文的期刊名，不允许与其他scope共存</br>
                    
                    允许添加filter的字段：</br>
                    数值型：<b>year</b> (below, above, range, equal), <b>citationNum</b> (above)</br>
                    离散型：<b>subjects</b>, <b>type</b>, <b>authors.name</b>, <b>institutions.name</b>, <b>journal.title</b>
                    
                    允许设置sort的字段：<b>date.asc</b>（出版时间正序）, <b>date.asc</b>（出版时间倒序，最新论文）, <b>citationNum.desc</b>（被引量倒序，最高被引）""")
    public Result<HitPage<PaperItem>> searchPapers(@RequestBody @Valid SearchRequest searchRequest,
                                                   HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<PaperItem>> result = new Result<>();
        HitPage<PaperItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();
        List<Filter> filters = searchRequest.getFilters();
        String srt = searchRequest.getSort(); // Sort

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("条件复合不能超过" + maxDepth + "层");

        /* All fields that are allowed to be contained in the scope */
        Set<String> allAllowedScopes = Set.of("title", "abstract", "keywords", "subjects", "type", "authors.name", "institutions.name", "journal.title");
        /* Fields that are not allowed to appear with any other field */
        Set<String> soloFieldScopes = Set.of("type", "authors.name", "institutions.name", "journal.title");

        // Hierarchical iteration
        Queue<Condition> checkQueue = new LinkedList<>(conditions);
        while (!checkQueue.isEmpty()) {
            Condition condition = checkQueue.remove();
            if (condition.isCompound()) {
                checkQueue.addAll(condition.getSubConditions());
                continue;
            }
            // Strip the keyword
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 128));

            // Scope validation
            Set<String> scope = condition.getScope();
            // Single scope constraints check
            if (!validateScope(scope, allAllowedScopes))
                return result.withFailure(ExceptionType.INVALID_PARAM);
            // Multiple scopes constraints check
            if (scope.size() > 1) {
                if (!Collections.disjoint(scope, soloFieldScopes))
                    return result.withFailure("非法的关键词范围组合");
            }
            if (scope.contains("type")) {
                if (condition.isFuzzy())
                    return result.withFailure("论文类别不可开启模糊搜索");
                if (condition.isTranslated())
                    return result.withFailure("论文类别不可开启语种关联");
            }
            if (scope.contains("authors.name")) {
                if (condition.isFuzzy())
                    return result.withFailure("作者姓名不可开启模糊搜索");
                if (condition.isTranslated())
                    return result.withFailure("作者姓名不可开启语种关联");
            }
        }

        // Param check for filters
        if (!validatePaperFilters(filters)) {
            return result.withFailure(ExceptionType.INVALID_PARAM);
        }

        // Sort check
        if (srt != null) {
            switch (srt) {
                case "date.asc", "date.desc", "citationNum.desc" -> {}
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "paper");
        QueryBuilder filter = searchService.buildFilter(filters, "paper");
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight("title", "title.raw", "abstract", "keywords", "keywords.raw");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Store to cache
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("index", "paper");
        session.setAttribute("query", query.toString());
        if (filter != null)
            session.setAttribute("filter", filter.toString());

        // Run search
        SearchHits<Paper> hits = searchService.runSearch(Paper.class, query, filter, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reducePaperHits(hits, hltConfig.preTag(), hltConfig.postTag()));
        hitPage.setStatistics(hits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        // Time cost
        hitPage.setTimeCost(System.currentTimeMillis() - start);

        return result.withData(hitPage);
    }

    @PostMapping("/researcher")
    @ApiOperation(
            value = "科研人员检索",
            notes = """
                    使用多个条件或嵌套条件搜索科研人员。
                    
                    条件中的scope参数允许的所有值的含义与限制规则如下：</br>
                    <b>name</b> - 科研人员姓名，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    <b>interests</b> - 科研人员研究方向，不允许与其他scope共存</br>
                    <b>currentInst.name</b> - 科研人员所属的机构名</br>
                    <b>institutions.name</b> - 科研人员的合作机构名</br>
                    
                    允许添加filter的字段：</br>
                    数值型：<b>hIndex</b>, <b>gIndex</b>, <b>paperNum</b>, <b>patentNum</b>, <b>citationNum</b>（type均只能是above）</br>
                    离散型：<b>interests</b>, <b>currentInst.name</b>
                    
                    允许设置sort的字段：<b>hIndex.desc</b>, <b>gIndex.desc</b>, <b>paperNum.desc</b>, <b>patentNum.desc</b>, <b>citationNum.desc</b>""")
    public Result<HitPage<ResearcherItem>> searchResearcher(@RequestBody @Valid SearchRequest searchRequest,
                                                            HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<ResearcherItem>> result = new Result<>();
        HitPage<ResearcherItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();
        List<Filter> filters = searchRequest.getFilters();
        String srt = searchRequest.getSort(); // Sort

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("条件复合不能超过" + maxDepth + "层");

        /* All fields that are allowed to be contained in the scope */
        Set<String> allAllowedScopes = Set.of("name", "interests", "currentInst.name", "institutions.name");

        // Hierarchical iteration
        Queue<Condition> checkQueue = new LinkedList<>(conditions);
        while (!checkQueue.isEmpty()) {
            Condition condition = checkQueue.remove();
            if (condition.isCompound()) {
                checkQueue.addAll(condition.getSubConditions());
                continue;
            }
            // Strip the keyword
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 128));

            // Scope validation
            Set<String> scope = condition.getScope();
            // Single scope constraints check
            if (!validateScope(scope, allAllowedScopes))
                return result.withFailure(ExceptionType.INVALID_PARAM);
            // Multiple scopes constraints check
            if (scope.size() > 1) {
                if (scope.contains("name") && scope.contains("interests"))
                    return result.withFailure("非法的关键词范围组合");
            }
            if (scope.contains("name")) {
                if (condition.isFuzzy())
                    return result.withFailure("学者姓名不可开启模糊搜索");
                if (condition.isTranslated())
                    return result.withFailure("学者姓名不可开启语种关联");
            }
        }

        // Param check for filters
        for (Filter filter : filters) {
            FilterFormat format = filter.getFormat();
            FilterType type = filter.getType();
            switch (format) {
                case NUMERIC -> {
                    if (!type.equals(FilterType.ABOVE))
                        return result.withFailure(ExceptionType.INVALID_PARAM);
                    switch (filter.getAttr()) {
                        case "hIndex", "gIndex", "paperNum", "patentNum", "citationNum" -> {}
                        default -> {
                            return result.withFailure(ExceptionType.INVALID_PARAM);
                        }
                    }
                }
                case DISCRETE -> {
                    switch (filter.getAttr()) {
                        case "interests", "currentInst.name" -> {}
                        default -> {
                            return result.withFailure(ExceptionType.INVALID_PARAM);
                        }
                    }
                }
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Sort check
        if (srt != null) {
            switch (srt) {
                case "hIndex.desc", "gIndex.desc", "paperNum.desc", "patentNum.desc", "citationNum.desc" -> {}
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "researcher");
        QueryBuilder filter = searchService.buildFilter(filters, "researcher");
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight(
                "name", "interests", "interests.raw", "currentInst.name", "currentInst.name.raw");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Store to cache
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("index", "researcher");
        session.setAttribute("query", query.toString());
        if (filter != null)
            session.setAttribute("filter", filter.toString());

        // Run search
        SearchHits<Researcher> hits = searchService.runSearch(Researcher.class, query, filter, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reduceResearcherHits(hits, hltConfig.preTag(), hltConfig.postTag()));
        hitPage.setStatistics(hits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        // Time cost
        hitPage.setTimeCost(System.currentTimeMillis() - start);

        return result.withData(hitPage);
    }

    @PostMapping("/journal")
    @ApiOperation(
            value = "学术期刊检索",
            notes = """
                    使用多个条件或嵌套条件搜索学术期刊。
                    
                    条件中的scope参数允许的所有值的含义与限制规则如下：</br>
                    <b>title</b> - 学术期刊标题</br>
                    
                    不支持添加任何filter。
                    
                    不支持设置任何sort。""")
    public Result<HitPage<JournalItem>> searchJournal(@RequestBody @Valid SearchRequest searchRequest,
                                                      HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<JournalItem>> result = new Result<>();
        HitPage<JournalItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("条件复合不能超过" + maxDepth + "层");

        // Hierarchical iteration
        Queue<Condition> checkQueue = new LinkedList<>(conditions);
        while (!checkQueue.isEmpty()) {
            Condition condition = checkQueue.remove();
            if (condition.isCompound()) {
                checkQueue.addAll(condition.getSubConditions());
                continue;
            }
            // Strip the keyword
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 128));

            // Scope validation
            Set<String> scope = condition.getScope();
            if (!scope.contains("title") || scope.size() > 1)
                return result.withFailure(ExceptionType.INVALID_PARAM);
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "journal");
        SortBuilder<?> sort = SortBuilders.scoreSort();
        HighlightBuilder hlt = searchService.buildHighlight("title", "title.raw");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Store to cache
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("index", "journal");
        session.setAttribute("query", query.toString());


        // Run search
        SearchHits<Journal> hits = searchService.runSearch(Journal.class, query, null, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reduceJournalHits(hits));
        hitPage.setStatistics(hits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        // Time cost
        hitPage.setTimeCost(System.currentTimeMillis() - start);

        return result.withData(hitPage);
    }

    @PostMapping("/institution")
    @ApiOperation(
            value = "学术机构检索",
            notes = """
                    使用多个条件或嵌套条件搜索学术机构。
                    
                    条件中的scope参数允许的所有值的含义与限制规则如下：</br>
                    <b>name</b> - 科研机构名称</br>
                    
                    不支持添加任何filter。
                    
                    不支持设置任何sort。""")
    public Result<HitPage<InstitutionItem>> searchInstitution(@RequestBody @Valid SearchRequest searchRequest,
                                                              HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<InstitutionItem>> result = new Result<>();
        HitPage<InstitutionItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("条件复合不能超过" + maxDepth + "层");

        // Hierarchical iteration
        Queue<Condition> checkQueue = new LinkedList<>(conditions);
        while (!checkQueue.isEmpty()) {
            Condition condition = checkQueue.remove();
            if (condition.isCompound()) {
                checkQueue.addAll(condition.getSubConditions());
                continue;
            }
            // Strip the keyword
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 128));

            // Scope validation
            Set<String> scope = condition.getScope();
            if (!scope.contains("name") || scope.size() > 1)
                return result.withFailure(ExceptionType.INVALID_PARAM);
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "institution");
        SortBuilder<?> sort = SortBuilders.scoreSort();
        HighlightBuilder hlt = searchService.buildHighlight("name", "name.raw");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Store to cache
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("index", "institution");
        session.setAttribute("query", query.toString());

        // Run search
        SearchHits<Institution> hits = searchService.runSearch(Institution.class, query, null, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reduceInstitutionHits(hits));
        hitPage.setStatistics(hits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        // Time cost
        hitPage.setTimeCost(System.currentTimeMillis() - start);

        return result.withData(hitPage);
    }

    @PostMapping("/patent")
    @ApiOperation(
            value = "专利检索",
            notes = """
                    使用多个条件或嵌套条件搜索专利。
                    
                    条件中的scope参数允许的所有值的含义与限制规则如下：</br>
                    <b>title</b> - 专利标题</br>
                    <b>type</b> - 专利类型，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    <b>abstract</b> - 专利摘要</br>
                    <b>applicant</b> - 专利申请人，不允许与其他scope共存</br>
                    <b>inventors.name</b> - 专利发明人，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    
                    允许添加filter的字段：</br>
                    数值型：无</br>
                    离散型：<b>type</b>, <b>inventors.name</b>, <b>applicant</b>
                    
                    允许设置sort的字段：<b>fillingDate.desc</b>, <b>fillingDate.asc</b>（即申请日期正序或倒序）""")
    public Result<HitPage<PatentItem>> searchPatent(@RequestBody @Valid SearchRequest searchRequest,
                                                    HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<PatentItem>> result = new Result<>();
        HitPage<PatentItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();
        List<Filter> filters = searchRequest.getFilters();
        String srt = searchRequest.getSort();

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("条件复合不能超过" + maxDepth + "层");
        /* All fields that are allowed to be contained in the scope */
        Set<String> allAllowedScopes = Set.of("title", "type", "abstract", "applicant", "inventors.name");
        /* Fields that are not allowed to appear with any other field */
        Set<String> soloFieldScopes = Set.of("type", "applicant", "inventors.name");

        // Hierarchical iteration
        Queue<Condition> checkQueue = new LinkedList<>(conditions);
        while (!checkQueue.isEmpty()) {
            Condition condition = checkQueue.remove();
            if (condition.isCompound()) {
                checkQueue.addAll(condition.getSubConditions());
                continue;
            }
            // Strip the keyword
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 128));

            // Scope validation
            Set<String> scope = condition.getScope();
            // Single scope constraints check
            if (!validateScope(scope, allAllowedScopes))
                return result.withFailure(ExceptionType.INVALID_PARAM);
            // Multiple scopes constraints check
            if (scope.size() > 1) {
                if (!Collections.disjoint(scope, soloFieldScopes))
                    return result.withFailure("非法的关键词范围组合");
            }
            if (scope.contains("type")) {
                if (condition.isFuzzy())
                    return result.withFailure("发明人姓名不可开启模糊搜索");
                if (condition.isTranslated())
                    return result.withFailure("发明人姓名不可开启语种关联");
            }
            if (scope.contains("inventors.name")) {
                if (condition.isFuzzy())
                    return result.withFailure("发明人姓名不可开启模糊搜索");
                if (condition.isTranslated())
                    return result.withFailure("发明人姓名不可开启语种关联");
            }
        }

        // Param check for filters
        for (Filter filter : filters) {
            if (!filter.getFormat().equals(FilterFormat.DISCRETE))
                return result.withFailure(ExceptionType.INVALID_PARAM);
            switch (filter.getAttr()) {
                case "type", "inventors.name", "applicant" -> {}
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Sort check
        if (srt != null) {
            switch (srt) {
                case "fillingDate.asc", "fillingDate.desc" -> {}
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "patent");
        QueryBuilder filter = searchService.buildFilter(filters, "patent");
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight("title", "title.raw");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Store to cache
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("index", "patent");
        session.setAttribute("query", query.toString());
        if (filter != null)
            session.setAttribute("filter", filter.toString());

        // Run search
        SearchHits<Patent> hits = searchService.runSearch(Patent.class, query, filter, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reducePatentHits(hits, hltConfig.preTag(), hltConfig.postTag()));
        hitPage.setStatistics(hits.getTotalHits(), searchRequest.getPage(), searchRequest.getSize());

        // Time cost
        hitPage.setTimeCost(System.currentTimeMillis() - start);

        return result.withData(hitPage);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateDepth(List<Condition> conditions, int maxDepth) {
        if (maxDepth < 0)
            return false;
        for (Condition condition : conditions) {
            if (condition.isCompound() && !validateDepth(condition.getSubConditions(), maxDepth - 1))
                return false;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateScope(Set<String> scope, Set<String> allowed) {
        scope.retainAll(allowed);
        return !scope.isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validatePaperFilters(List<Filter> filters) {
        for (Filter filter : filters) {
            FilterFormat format = filter.getFormat();
            FilterType type = filter.getType();
            switch (format) {
                case NUMERIC -> {
                    switch (type) {
                        case BELOW, RANGE, EQUAL -> {
                            if (!filter.getAttr().equals("year"))
                                return false;
                        }
                        case ABOVE -> {
                            if (!filter.getAttr().matches("^(year)|(citationNum)$"))
                                return false;
                        }
                        default -> {
                            return false;
                        }
                    }
                }
                case DISCRETE -> {
                    switch (filter.getAttr()) {
                        case "subjects", "type", "authors.name", "institutions.name", "journal.title" -> {}
                        default -> {
                            return false;
                        }
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

}
