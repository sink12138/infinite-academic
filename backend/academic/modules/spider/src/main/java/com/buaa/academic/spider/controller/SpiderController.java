package com.buaa.academic.spider.controller;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidHeader(String auth) {
        return auth.equals(authHeader);
    }

    @Autowired
    private StatusCtrl statusCtrl;

    @PostMapping("/start")
    public Result<Void> start(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        statusCtrl.setQueueInitThreadNum(2);
        statusCtrl.setMainInfoThreadNum(4);
        statusCtrl.setPaperSourceThreadNum(4);
        statusCtrl.setResearcherThreadNum(4);
        statusCtrl.setInterestsThreadNum(1);
        statusCtrl.setJournalThreadNum(1);
        statusCtrl.setSubjectTopicThreadNum(2);
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
        StatusCtrl.keywordQueue.addAll(keywords);
        return result;
    }

    @Autowired
    private ElasticsearchRestTemplate template;

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

    @PostMapping("/crawlAuId")
    public Result<Void> crawlAuId(@RequestHeader(name = "Auth") String auth) {
        Result<Void> result = new Result<>();
        if (!isValidHeader(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        statusCtrl.setQueueInitThreadNum(3);
        statusCtrl.setPaperSourceThreadNum(2);
        statusCtrl.setMainInfoThreadNum(5);
        statusCtrl.setResearcherThreadNum(7);
        statusCtrl.setSubjectTopicThreadNum(1);
        statusCtrl.setJournalThreadNum(1);
        statusCtrl.setInterestsThreadNum(1);
        if (statusCtrl.fixResearcherId())
            return result;
        return result.withFailure("Has been running");
    }
}
