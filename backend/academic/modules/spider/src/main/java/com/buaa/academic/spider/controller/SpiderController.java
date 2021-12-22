package com.buaa.academic.spider.controller;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import com.buaa.academic.spider.service.CrawlService;
import com.buaa.academic.spider.util.StatusCtrl;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Validated
@RestController
public class SpiderController {
    private String authHeader;

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String password;

    @PostConstruct
    public void init() {
        if (authHeader == null)
            authHeader = Base64.getEncoder().encodeToString((username + '@' + password).getBytes(StandardCharsets.UTF_8));
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidHeader(String auth) {
        return auth.equals(authHeader);
    }

    @Autowired
    private StatusCtrl statusCtrl;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private CrawlService crawlService;

    @PostMapping("/start")
    public Result<Void> start(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        statusCtrl.setPapersInitThreadNum(0);
        statusCtrl.setPaperMainInfoNum(0);
        statusCtrl.setPaperSourceThreadNum(0);
        statusCtrl.setResearcherThreadNum(0);
        statusCtrl.setInterestsThreadNum(0);
        statusCtrl.setJournalThreadNum(0);
        statusCtrl.setSubjectTopicThreadNum(0);
        statusCtrl.setPatentsInitThreadNum(2);
        statusCtrl.setPatentMainInfoNum(2);
        if (statusCtrl.start())
            return result;
        return result.withFailure("Has been running");
    }

    @PostMapping("/stop")
    public Result<Void> stop(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        if (!statusCtrl.isRunning())
            return result.withFailure("Has stopped");
        statusCtrl.stop();
        return result;
    }

    @GetMapping("/status")
    public Result<Schedule> getStatus(@RequestHeader(name = "Auth") String auth) {
        Result<Schedule> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        return result.withData(statusCtrl.getStatus());
    }

    @PostMapping("/setting")
    public Result<Void> setting(@RequestHeader(name = "Auth") String auth,
                                @RequestBody List<String> keywords) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        StatusCtrl.paperKeywordQueue.addAll(keywords);
        return result;
    }

    @PostMapping("/fix")
    public Result<Void> fix() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.existsQuery("institutions"))
                        .mustNot(QueryBuilders.existsQuery("authors.id")))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(10000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 10000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                List<Paper.Author> authors = paper.getAuthors();
                for (Paper.Author author : authors) {
                    boolean found = false;
                    for (Paper.Institution institution : paper.getInstitutions()) {
                        SearchHits<Researcher> researcherHits = template.search(new NativeSearchQueryBuilder()
                                .withQuery(QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("name", author.getName()))
                                        .must(QueryBuilders.matchQuery("currentInst.name", institution.getName())))
                                .withMaxResults(5)
                                .build(), Researcher.class);
                        for (SearchHit<Researcher> researcherHit : researcherHits) {
                            Researcher researcher = researcherHit.getContent();
                            if (researcher.getCurrentInst().getName().startsWith(institution.getName()) || institution.getName().startsWith(researcher.getCurrentInst().getName())) {
                                author.setId(researcher.getId());
                                edited = true;
                                found = true;
                                break;
                            }
                        }
                        if (found)
                            break;
                    }
                }
                if (edited) {
                    ++fixed;
                    template.save(paper);
                }
            }
            log.info("Scrolled {}, fixed {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    @PostMapping("/dedup")
    public Result<Void> dedup() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.existsQuery("sources"))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(10000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 10000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                List<Paper.Source> sources = paper.getSources();
                for (int i = 0; i < sources.size(); ++i) {
                    for (int j = i + 1; j < sources.size();) {
                        if (sources.get(i).getWebsite().equals(sources.get(j).getWebsite())) {
                            edited = true;
                            sources.remove(j);
                        }
                        else {
                            ++j;
                        }
                    }
                }
                if (edited) {
                    ++fixed;
                    template.save(paper);
                }
            }
            log.info("Scrolled {}, fixed {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    @PostMapping("/strip")
    public Result<Void> strip() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.existsQuery("journal.volume"))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(10000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 10000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                Paper.Journal journal = paper.getJournal();
                String volume = journal.getVolume();
                if (volume.startsWith(" ")) {
                    if (volume.isBlank())
                        journal.setVolume(null);
                    else
                        journal.setVolume(volume.strip());
                    edited = true;
                }
                if (edited) {
                    ++fixed;
                    template.save(paper);
                }
            }
            log.info("Scrolled {}, fixed {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    @PostMapping("/split")
    public Result<Void> split() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.existsQuery("keywords"))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(10000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 10000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                List<String> keywords = new ArrayList<>();
                for (String keyword : paper.getKeywords()) {
                    if (keyword.contains("，") || keyword.contains(",")) {
                        edited = true;
                        for (String term : keyword.split("[,，]+")) {
                            if (!term.isBlank())
                                keywords.add(term.strip());
                        }
                    }
                    else {
                        keywords.add(keyword);
                    }
                }
                if (edited) {
                    ++fixed;
                    paper.setKeywords(keywords);
                    template.save(paper);
                }
            }
            log.info("Scrolled {}, fixed {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    @PostMapping("/gm1")
    public Result<Void> gm1() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("keywords.raw", "1)模型"))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(10000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 10000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                List<String> keywords = paper.getKeywords();
                for (int i = 0; i < keywords.size(); ++i) {
                    if (keywords.get(i).matches("^.*\\(\\d$")) {
                        keywords.set(i, keywords.get(i) + ",1)模型");
                        keywords.remove(i + 1);
                        edited = true;
                        break;
                    }
                }
                if (edited) {
                    ++fixed;
                    template.save(paper);
                }
            }
            log.info("Scrolled {}, fixed {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    @PostMapping("/crawlAuId")
    public Result<Void> crawlAuId(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        statusCtrl.setPapersInitThreadNum(3);
        statusCtrl.setPaperSourceThreadNum(2);
        statusCtrl.setPaperMainInfoNum(5);
        statusCtrl.setResearcherThreadNum(7);
        statusCtrl.setSubjectTopicThreadNum(1);
        statusCtrl.setJournalThreadNum(1);
        statusCtrl.setInterestsThreadNum(1);
        if (statusCtrl.fixResearcherId())
            return result;
        return result.withFailure("Has been running");
    }

    @PostMapping("/crawlWithUrl")
    public Result<Void> crawlWithUrl(@RequestHeader(name = "Auth") String userId,
                                     @RequestParam(name = "url") @NotBlank String url) {
        Result<Void> result = new Result<>();
        if (!template.exists(userId, User.class))
            return result.withFailure(ExceptionType.NOT_FOUND);
        crawlService.crawlWithUrl(url, userId);
        return result;
    }

    @PostMapping("/crawlWithTitle")
    public Result<Void> crawlWithTitle(@RequestHeader(name = "Auth") String userId,
                                       @RequestParam(name = "title") @NotBlank String title) {
        Result<Void> result = new Result<>();
        if (!template.exists(userId, User.class))
            return result.withFailure(ExceptionType.NOT_FOUND);
        crawlService.crawlWithTitle(title, userId);
        return result;
    }
}
