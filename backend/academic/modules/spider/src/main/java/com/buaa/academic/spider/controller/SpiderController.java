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
        statusCtrl.setMainInfoThreadNum(3);
        statusCtrl.setPaperSourceThreadNum(2);
        statusCtrl.setResearcherThreadNum(2);
        statusCtrl.setInterestsThreadNum(4);
        statusCtrl.setJournalThreadNum(1);
        statusCtrl.setSubjectTopicThreadNum(3);
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
    public Result<Void> scroll() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.existsQuery("sources"))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(2000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 2000, Paper.class, IndexCoordinates.of("paper"))) {
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

}
