package com.buaa.academic.spider.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.spider.service.AminerService;
import com.buaa.academic.spider.util.StringUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MaintenanceController {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private AminerService aminerService;

    @PostMapping("/hybrid")
    public Result<Void> hybrid() {
        new Thread(() -> {
            try {
                rename();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            inst();
            fix();
            count();
        }, "Thread-hybrid").start();
        return new Result<>();
    }

    @PostMapping("/fix")
    public Result<Void> fix() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("authors.id")))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(60000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 60000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                List<Paper.Author> authors = paper.getAuthors();
                for (Paper.Author author : authors) {
                    if (paper.getInstitutions() == null) {
                        SearchHits<Researcher> researcherHits = template.search(new NativeSearchQueryBuilder()
                                .withQuery(QueryBuilders.termQuery("name", author.getName()))
                                .withMaxResults(1)
                                .build(), Researcher.class);
                        if (researcherHits.getTotalHits() == 1) {
                            Researcher researcher = researcherHits.getSearchHit(0).getContent();
                            author.setId(researcher.getId());
                            researcher.setPaperNum(researcher.getPaperNum() + 1);
                            template.save(researcher);
                            edited = true;
                        }
                    }
                    else {
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
                                    researcher.setPaperNum(researcher.getPaperNum() + 1);
                                    template.save(researcher);
                                    edited = true;
                                    found = true;
                                    break;
                                }
                            }
                            if (found)
                                break;
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

    @PostMapping("/replace")
    public Result<Void> replace() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.existsQuery("agent"))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Patent> scrollHits = template.searchScrollStart(10000, query, Patent.class, IndexCoordinates.of("patent"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 10000, Patent.class, IndexCoordinates.of("patent"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Patent> hit : scrollHits) {
                Patent patent = hit.getContent();
                String agent = patent.getAgent();
                boolean edited = false;
                if (agent.contains("%")) {
                    patent.setAgent(agent.replaceAll("%", "; "));
                    edited = true;
                }
                if (edited) {
                    ++fixed;
                    template.save(patent);
                }
            }
            log.info("Scrolled {}, fixed {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    @PostMapping("/count")
    public Result<Void> count() {
        int sum;
        int fixed;
        int deleted;
        Query query;
        Logger log = LoggerFactory.getLogger(this.getClass());

        /* Citation num of papers */
        query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(0, 1000))
                .build();
        sum = 0;
        fixed = 0;
        for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(20000, query, Paper.class, IndexCoordinates.of("paper"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 20000, Paper.class, IndexCoordinates.of("paper"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Paper> hit : scrollHits) {
                Paper paper = hit.getContent();
                boolean edited = false;
                int citationNum = (int) template.count(new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termQuery("references", paper.getId()))
                        .build(), Paper.class);
                if (citationNum != paper.getCitationNum()) {
                    paper.setCitationNum(citationNum);
                    edited = true;
                }
                if (edited) {
                    ++fixed;
                    template.save(paper);
                }
            }
            log.info("Scrolled {}, fixed {} paper(s)", sum, fixed);
        }
        log.info("Scroll papers done");

        /* Paper num of researchers */
        query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(0, 1000))
                .build();
        sum = 0;
        fixed = 0;
        deleted = 0;
        for (SearchScrollHits<Researcher> scrollHits = template.searchScrollStart(20000, query, Researcher.class, IndexCoordinates.of("researcher"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 20000, Researcher.class, IndexCoordinates.of("researcher"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Researcher> hit : scrollHits) {
                Researcher researcher = hit.getContent();
                boolean edited = false;
                int paperNum = (int) template.count(new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termQuery("authors.id", researcher.getId()))
                        .build(), Paper.class);
                if (paperNum != researcher.getPaperNum()) {
                    researcher.setPaperNum(paperNum);
                    edited = true;
                }
                if (paperNum == 0) {
                    SearchHit<User> certified = template.searchOne(new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.termQuery("researcherId", researcher.getId()))
                            .build(), User.class);
                    if (certified == null) {
                        template.delete(researcher);
                        ++deleted;
                        continue;
                    }
                }
                if (edited) {
                    ++fixed;
                    template.save(researcher);
                }
            }
            log.info("Scrolled {}, fixed {}, deleted {} researcher(s)", sum, fixed, deleted);
        }
        log.info("Scroll researchers done");

        return new Result<>();
    }

    @PostMapping("/rename")
    public Result<Void> rename() throws InterruptedException {
        Query firstQuery;
        int firstSum;
        int firstFixed;
        Logger log = LoggerFactory.getLogger(this.getClass());
        firstQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(0, 1000))
                .build();
        firstSum = 0;
        firstFixed = 0;
        for (SearchScrollHits<Institution> scrollHits = template.searchScrollStart(20000, firstQuery, Institution.class, IndexCoordinates.of("institution"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 20000, Institution.class, IndexCoordinates.of("institution"))) {
            firstSum += scrollHits.getSearchHits().size();
            for (SearchHit<Institution> hit : scrollHits) {
                Institution institution = hit.getContent();
                boolean edited = false;
                String newName = StringUtil.formatInstitutionName(institution.getName());
                if (!newName.equals(institution.getName())) {
                    institution.setName(newName);
                    edited = true;
                }
                if (edited) {
                    ++firstFixed;
                    template.save(institution);
                }
            }
            log.info("Scrolled {}, renamed {} institutions", firstSum, firstFixed);
        }
        log.info("Scroll institutions done");

        Thread paperThread = new Thread(() -> {
            Query query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.existsQuery("institutions"))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();
            int sum = 0;
            int fixed = 0;
            int created = 0;
            for (SearchScrollHits<Paper> scrollHits = template.searchScrollStart(20000, query, Paper.class, IndexCoordinates.of("paper"));
                 scrollHits.hasSearchHits();
                 scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 20000, Paper.class, IndexCoordinates.of("paper"))) {
                sum += scrollHits.getSearchHits().size();
                for (SearchHit<Paper> hit : scrollHits) {
                    Paper paper = hit.getContent();
                    boolean edited = false;
                    for (Paper.Institution institution : paper.getInstitutions()) {
                        Institution inst = template.get(institution.getId(), Institution.class);
                        if (inst != null) {
                            if (!inst.getName().equals(institution.getName())) {
                                institution.setName(inst.getName());
                                edited = true;
                            }
                        }
                        else {
                            String instName = StringUtil.formatInstitutionName(institution.getName());
                            institution.setId(aminerService.findInstitution(instName).getId());
                            ++created;
                            if (!instName.equals(institution.getName())) {
                                institution.setName(instName);
                                edited = true;
                            }
                        }
                    }
                    if (edited) {
                        template.save(paper);
                        ++fixed;
                    }
                }
                log.info("Scrolled {}, renamed {}, created {} institutions of papers", sum, fixed, created);
            }
            log.info("Scroll papers done");
        });
        paperThread.start();

        Thread researcherThread = new Thread(() -> {
            Query query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery()
                            .should(QueryBuilders.existsQuery("currentInst"))
                            .should(QueryBuilders.existsQuery("institutions")))
                    .withPageable(PageRequest.of(0, 1000))
                    .build();
            int sum = 0;
            int fixed = 0;
            int created = 0;
            for (SearchScrollHits<Researcher> scrollHits = template.searchScrollStart(20000, query, Researcher.class, IndexCoordinates.of("researcher"));
                 scrollHits.hasSearchHits();
                 scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 20000, Researcher.class, IndexCoordinates.of("researcher"))) {
                sum += scrollHits.getSearchHits().size();
                for (SearchHit<Researcher> hit : scrollHits) {
                    Researcher researcher = hit.getContent();
                    boolean edited = false;
                    if (researcher.getCurrentInst() != null) {
                        Researcher.Institution institution = researcher.getCurrentInst();
                        Institution inst = template.get(institution.getId(), Institution.class);
                        if (inst != null) {
                            if (!inst.getName().equals(institution.getName())) {
                                institution.setName(inst.getName());
                                edited = true;
                            }
                        }
                        else {
                            String instName = StringUtil.formatInstitutionName(institution.getName());
                            institution.setId(aminerService.findInstitution(instName).getId());
                            if (!instName.equals(institution.getName())) {
                                institution.setName(instName);
                                edited = true;
                            }
                        }
                    }
                    if (researcher.getInstitutions() != null) {
                        for (Researcher.Institution institution : researcher.getInstitutions()) {
                            Institution inst = template.get(institution.getId(), Institution.class);
                            if (inst != null) {
                                if (!inst.getName().equals(institution.getName())) {
                                    institution.setName(inst.getName());
                                    edited = true;
                                }
                            }
                            else {
                                String instName = StringUtil.formatInstitutionName(institution.getName());
                                institution.setId(aminerService.findInstitution(instName).getId());
                                if (!instName.equals(institution.getName())) {
                                    institution.setName(instName);
                                    edited = true;
                                }
                            }
                        }
                    }
                    if (edited) {
                        template.save(researcher);
                        ++fixed;
                    }
                }
                log.info("Scrolled {}, renamed {}, created {} institutions of researchers", sum, fixed, created);
            }
            log.info("Scroll researchers done");
        });
        researcherThread.start();
        paperThread.join();
        researcherThread.join();
        return new Result<>();
    }

    @PostMapping("/inst")
    public Result<Void> inst() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int sum = 0;
        int fixed = 0;
        Logger log = LoggerFactory.getLogger(this.getClass());
        for (SearchScrollHits<Institution> scrollHits = template.searchScrollStart(60000, query, Institution.class, IndexCoordinates.of("institution"));
             scrollHits.hasSearchHits();
             scrollHits = template.searchScrollContinue(scrollHits.getScrollId(), 60000, Institution.class, IndexCoordinates.of("institution"))) {
            sum += scrollHits.getSearchHits().size();
            for (SearchHit<Institution> hit : scrollHits) {
                Institution institution = hit.getContent();
                if (!template.exists(institution.getId(), Institution.class))
                    continue;
                int merged = mergeInstitution(institution);
                if (merged > 0) {
                    fixed += merged;
                }
            }
            log.info("Scrolled {}, merged {}", sum, fixed);
        }
        log.info("Scroll done");
        return new Result<>();
    }

    private int mergeInstitution(Institution institution) {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.termQuery("name.raw", institution.getName()))
                        .should(QueryBuilders.termQuery("name.raw", institution.getName().replaceAll(",", "")))
                        .should(QueryBuilders.termQuery("name.raw", institution.getName().toLowerCase()))
                        .should(QueryBuilders.termQuery("name.raw", institution.getName().toUpperCase())))
                .withPageable(PageRequest.of(0, 1000))
                .build();
        int merged = 0;
        SearchHits<Institution> searchHits = template.search(query, Institution.class);
        for (SearchHit<Institution> hit : searchHits) {
            Institution inst = hit.getContent();
            if (inst.getId().equals(institution.getId()))
                continue;
            if (!template.exists(inst.getId(), Institution.class))
                continue;
            template.updateByQuery(UpdateQuery
                    .builder(new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.termQuery("institutions.id", inst.getId()))
                            .build())
                    .withScript("for (int i = 0; i < ctx._source.institutions.size(); ++i) { if (ctx._source.institutions[i].id.equals(params.before)) ctx._source.institutions[i].id = params.after; }")
                    .withParams(Map.of("before", inst.getId(), "after", institution.getId()))
                    .withScriptType(ScriptType.INLINE)
                    .build(), IndexCoordinates.of("paper"));
            template.updateByQuery(UpdateQuery
                    .builder(new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.termQuery("currentInst.id", inst.getId()))
                            .build())
                    .withScript("ctx._source.currentInst.id = params.after;")
                    .withParams(Map.of("after", institution.getId()))
                    .withScriptType(ScriptType.INLINE)
                    .build(), IndexCoordinates.of("researcher"));
            template.updateByQuery(UpdateQuery
                    .builder(new NativeSearchQueryBuilder()
                            .withQuery(QueryBuilders.termQuery("institutions.id", inst.getId()))
                            .build())
                    .withScript("for (int i = 0; i < ctx._source.institutions.size(); ++i) { if (ctx._source.institutions[i].id.equals(params.before)) ctx._source.institutions[i].id = params.after; }")
                    .withParams(Map.of("before", inst.getId(), "after", institution.getId()))
                    .withScriptType(ScriptType.INLINE)
                    .build(), IndexCoordinates.of("researcher"));
            ++merged;
            template.delete(inst);
        }
        return merged;
    }

}
