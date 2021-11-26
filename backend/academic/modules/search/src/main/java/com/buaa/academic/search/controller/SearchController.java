package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.document.entity.item.*;
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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
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
import java.util.*;

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
                    "允许添加filter的字段：year (below, above, range, equal), citationNum (above)")
    public Result<SmartPage> smartSearch(@RequestBody @Valid SmartSearchRequest searchRequest) {
        long start = System.currentTimeMillis();
        Result<SmartPage> result = new Result<>();
        SmartPage smartPage = new SmartPage();

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

        // Strip keyword
        String keyword = StringUtils.strip(searchRequest.getKeyword(), 64);

        // TODO 2021/11/27: Add auto-correction feature

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
            keywords = new String[]{ keyword };
        }

        // Filters
        QueryBuilder filter = searchService.buildFilter(filters);

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
            notes = """
                    使用多个条件或嵌套条件搜索学术论文。
                    
                    条件中的scope参数允许的所有值的含义与限制规则如下：</br>
                    <b>title</b> - 论文标题</br>
                    <b>abstract</b> - 论文摘要</br>
                    <b>keywords</b> - 论文关键词</br>
                    <b>subjects</b> - 论文学科分类</br>
                    <b>topics</b> - 论文话题分类</br>
                    <b>type</b> - 论文本身的类别，如学位论文、期刊论文等，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    <b>authors.name</b> - 论文作者的姓名，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>
                    <b>institutions.name</b> - 发表论文的机构名，不允许与其他scope共存</br>
                    <b>journal.title</b> - 刊登论文的期刊名，不允许与其他scope共存</br>
                    
                    允许添加filter的字段：year (below, above, range, equal), citationNum (above)
                    
                    允许设置sort的字段：date.asc（出版时间正序）, date.asc（出版时间倒序，最新论文）, citationNum.desc（被引量倒序，最高被引）""")
    public Result<HitPage<PaperItem>> searchPapers(@RequestBody @Valid SearchRequest searchRequest) {
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
        Set<String> allAllowedScopes = Set.of("title", "abstract", "keywords", "subjects", "topics", "type", "authors.name", "institutions.name", "journal.title");
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
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 64));

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
        QueryBuilder filter = searchService.buildFilter(filters);
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight("title", "abstract", "keywords");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Paper> hits = searchService.advancedSearch(Paper.class, query, filter, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reducePaperHits(hits, preTag, postTag));
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
                    
                    允许添加filter的字段：hIndex, gIndex, paperNum, patentNum, citationNum（type均只能是above）
                    
                    允许设置sort的字段：hIndex.desc, gIndex.desc, paperNum.desc, patentNum.desc, citationNum.desc""")
    public Result<HitPage<ResearcherItem>> searchResearcher(@RequestBody @Valid SearchRequest searchRequest) {
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
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 64));

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
            if (!filter.getType().equals("above"))
                return result.withFailure(ExceptionType.INVALID_PARAM);
            switch (filter.getAttr()) {
                case "hIndex", "gIndex", "paperNum", "patentNum", "citationNum" -> {}
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
        QueryBuilder filter = searchService.buildFilter(filters);
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight("name", "interests");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Researcher> hits = searchService.advancedSearch(Researcher.class, query, filter, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reduceResearcherHits(hits));
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
    public Result<HitPage<JournalItem>> searchJournal(@RequestBody @Valid SearchRequest searchRequest) {
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
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 64));

            // Scope validation
            Set<String> scope = condition.getScope();
            if (!scope.contains("title") || scope.size() > 1)
                return result.withFailure(ExceptionType.INVALID_PARAM);
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "journal");
        SortBuilder<?> sort = SortBuilders.scoreSort();
        HighlightBuilder hlt = searchService.buildHighlight("title");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Journal> hits = searchService.advancedSearch(Journal.class, query, null, sort, hlt, page);

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
    public Result<HitPage<InstitutionItem>> searchInstitution(@RequestBody @Valid SearchRequest searchRequest) {
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
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 64));

            // Scope validation
            Set<String> scope = condition.getScope();
            if (!scope.contains("name") || scope.size() > 1)
                return result.withFailure(ExceptionType.INVALID_PARAM);
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "institution");
        SortBuilder<?> sort = SortBuilders.scoreSort();
        HighlightBuilder hlt = searchService.buildHighlight("name");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Institution> hits = searchService.advancedSearch(Institution.class, query, null, sort, hlt, page);

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
                    <b>abstract</b> - 专利摘要</br>
                    <b>applicant</b> - 专利申请人，不允许与其他scope共存</br>
                    <b>inventors.name</b> - 专利发明人，不允许开启模糊搜索和语种关联，且不允许与其他scope共存</br>

                    不支持添加任何filter。
                    
                    允许设置sort的字段：fillingDate.desc, fillingDate.asc（即申请日期正序或倒序）""")
    public Result<HitPage<PatentItem>> searchPatent(@RequestBody @Valid SearchRequest searchRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<PatentItem>> result = new Result<>();
        HitPage<PatentItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();
        String srt = searchRequest.getSort();

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("条件复合不能超过" + maxDepth + "层");
        /* All fields that are allowed to be contained in the scope */
        Set<String> allAllowedScopes = Set.of("title", "abstract", "applicant", "inventors.name");
        /* Fields that are not allowed to appear with any other field */
        Set<String> soloFieldScopes = Set.of("applicant", "inventors.name");

        // Hierarchical iteration
        Queue<Condition> checkQueue = new LinkedList<>(conditions);
        while (!checkQueue.isEmpty()) {
            Condition condition = checkQueue.remove();
            if (condition.isCompound()) {
                checkQueue.addAll(condition.getSubConditions());
                continue;
            }
            // Strip the keyword
            condition.setKeyword(StringUtils.strip(condition.getKeyword(), 64));

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
            if (scope.contains("inventors.name")) {
                if (condition.isFuzzy())
                    return result.withFailure("发明人姓名不可开启模糊搜索");
                if (condition.isTranslated())
                    return result.withFailure("发明人姓名不可开启语种关联");
            }
        }

        // Sort check
        if (srt != null) {
            switch (srt) {
                case "filingDate.asc", "filingDate.desc" -> {}
                default -> {
                    return result.withFailure(ExceptionType.INVALID_PARAM);
                }
            }
        }

        // Prepare search params
        QueryBuilder query = searchService.buildQuery(conditions, "patent");
        SortBuilder<?> sort = searchService.buildSort(srt);
        HighlightBuilder hlt = searchService.buildHighlight("title", "applicant");
        Pageable page = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Run search
        SearchHits<Patent> hits = searchService.advancedSearch(Patent.class, query, null, sort, hlt, page);

        // Items & statistics
        hitPage.setItems(HitsReducer.reducePatentHits(hits, preTag, postTag));
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

}
