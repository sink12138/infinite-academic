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
@Api(tags = "??????????????????")
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
            value = "???????????????",
            notes = "???????????????????????????????????????????????????????????????????????????????????????</br>" +
                    "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????</br>" +
                    "<b>????????????????????????????????????????????????</b>?????????????????????????????????????????????????????????")
    @ApiImplicitParam(name = "Target", value = "??????????????????????????????paper/researcher/journal/institution/patent")
    public void search(@RequestHeader("Target") @Valid @AllowValues({"smart", "paper", "researcher", "journal", "institution", "patent"}) String target,
                       HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/" + target).forward(request, response);
    }

    @PostMapping("/smart")
    @ApiOperation(
            value = "????????????",
            notes = "??????????????????????????????????????????</br>" +
                    "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????</br>" +
                    "????????????????????????????????????????????????????????????</br>" +
                    "???????????????filter???sort??????????????????????????????????????????")
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
            value = "??????????????????",
            notes = """
                    ??????????????????????????????????????????????????????
                    
                    ????????????scope?????????????????????????????????????????????????????????</br>
                    <b>title</b> - ????????????</br>
                    <b>abstract</b> - ????????????</br>
                    <b>keywords</b> - ???????????????</br>
                    <b>subjects</b> - ??????????????????</br>
                    <b>type</b> - ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????scope??????</br>
                    <b>authors.name</b> - ??????????????????????????????????????????????????????????????????????????????????????????scope??????</br>
                    <b>institutions.name</b> - ?????????????????????????????????????????????scope??????</br>
                    <b>journal.title</b> - ?????????????????????????????????????????????scope??????</br>
                    
                    ????????????filter????????????</br>
                    ????????????<b>year</b> (below, above, range, equal), <b>citationNum</b> (above)</br>
                    ????????????<b>subjects</b>, <b>type</b>, <b>authors.name</b>, <b>institutions.name</b>, <b>journal.title</b>
                    
                    ????????????sort????????????<b>date.asc</b>????????????????????????, <b>date.asc</b>???????????????????????????????????????, <b>citationNum.desc</b>????????????????????????????????????""")
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
            return result.withFailure("????????????????????????" + maxDepth + "???");

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
                    return result.withFailure("??????????????????????????????");
            }
            if (scope.contains("type")) {
                if (condition.isFuzzy())
                    return result.withFailure("????????????????????????????????????");
                if (condition.isTranslated())
                    return result.withFailure("????????????????????????????????????");
            }
            if (scope.contains("authors.name")) {
                if (condition.isFuzzy())
                    return result.withFailure("????????????????????????????????????");
                if (condition.isTranslated())
                    return result.withFailure("????????????????????????????????????");
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
            value = "??????????????????",
            notes = """
                    ??????????????????????????????????????????????????????
                    
                    ????????????scope?????????????????????????????????????????????????????????</br>
                    <b>name</b> - ???????????????????????????????????????????????????????????????????????????????????????scope??????</br>
                    <b>interests</b> - ?????????????????????????????????????????????scope??????</br>
                    <b>currentInst.name</b> - ??????????????????????????????</br>
                    <b>institutions.name</b> - ??????????????????????????????</br>
                    
                    ????????????filter????????????</br>
                    ????????????<b>hIndex</b>, <b>gIndex</b>, <b>paperNum</b>, <b>patentNum</b>, <b>citationNum</b>???type????????????above???</br>
                    ????????????<b>interests</b>, <b>currentInst.name</b>
                    
                    ????????????sort????????????<b>hIndex.desc</b>, <b>gIndex.desc</b>, <b>paperNum.desc</b>, <b>patentNum.desc</b>, <b>citationNum.desc</b>""")
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
            return result.withFailure("????????????????????????" + maxDepth + "???");

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
                    return result.withFailure("??????????????????????????????");
            }
            if (scope.contains("name")) {
                if (condition.isFuzzy())
                    return result.withFailure("????????????????????????????????????");
                if (condition.isTranslated())
                    return result.withFailure("????????????????????????????????????");
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
            value = "??????????????????",
            notes = """
                    ??????????????????????????????????????????????????????
                    
                    ????????????scope?????????????????????????????????????????????????????????</br>
                    <b>title</b> - ??????????????????</br>
                    
                    ?????????????????????filter???
                    
                    ?????????????????????sort???""")
    public Result<HitPage<JournalItem>> searchJournal(@RequestBody @Valid SearchRequest searchRequest,
                                                      HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<JournalItem>> result = new Result<>();
        HitPage<JournalItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("????????????????????????" + maxDepth + "???");

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
            value = "??????????????????",
            notes = """
                    ??????????????????????????????????????????????????????
                    
                    ????????????scope?????????????????????????????????????????????????????????</br>
                    <b>name</b> - ??????????????????</br>
                    
                    ?????????????????????filter???
                    
                    ?????????????????????sort???""")
    public Result<HitPage<InstitutionItem>> searchInstitution(@RequestBody @Valid SearchRequest searchRequest,
                                                              HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Result<HitPage<InstitutionItem>> result = new Result<>();
        HitPage<InstitutionItem> hitPage = new HitPage<>();

        List<Condition> conditions = searchRequest.getConditions();

        // Condition tree depth check
        if (!validateDepth(conditions, maxDepth))
            return result.withFailure("????????????????????????" + maxDepth + "???");

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
            value = "????????????",
            notes = """
                    ????????????????????????????????????????????????
                    
                    ????????????scope?????????????????????????????????????????????????????????</br>
                    <b>title</b> - ????????????</br>
                    <b>type</b> - ?????????????????????????????????????????????????????????????????????????????????scope??????</br>
                    <b>abstract</b> - ????????????</br>
                    <b>applicant</b> - ????????????????????????????????????scope??????</br>
                    <b>inventors.name</b> - ????????????????????????????????????????????????????????????????????????????????????scope??????</br>
                    
                    ????????????filter????????????</br>
                    ???????????????</br>
                    ????????????<b>type</b>, <b>inventors.name</b>, <b>applicant</b>
                    
                    ????????????sort????????????<b>fillingDate.desc</b>, <b>fillingDate.asc</b>????????????????????????????????????""")
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
            return result.withFailure("????????????????????????" + maxDepth + "???");
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
                    return result.withFailure("??????????????????????????????");
            }
            if (scope.contains("type")) {
                if (condition.isFuzzy())
                    return result.withFailure("???????????????????????????????????????");
                if (condition.isTranslated())
                    return result.withFailure("???????????????????????????????????????");
            }
            if (scope.contains("inventors.name")) {
                if (condition.isFuzzy())
                    return result.withFailure("???????????????????????????????????????");
                if (condition.isTranslated())
                    return result.withFailure("???????????????????????????????????????");
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
