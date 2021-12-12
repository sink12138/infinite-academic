package com.buaa.academic.admin.service.impl;

import com.buaa.academic.admin.service.ReviewService;
import com.buaa.academic.document.entity.*;
import com.buaa.academic.document.system.Trash;
import com.buaa.academic.model.application.PaperForApp;
import com.buaa.academic.model.application.PortalForApp;
import com.buaa.academic.model.application.Transfer;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ReviewServiceImpl implements ReviewService {
    
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public void savePaper(String paperIdIfExists, PaperForApp paperForApp) {
        new Thread(() -> {
            Paper paper = paperIdIfExists == null ? new Paper() : Objects.requireNonNull(template.get(paperIdIfExists, Paper.class));
            /* title */
            paper.setTitle(paperForApp.getTitle());
            /* type */
            paper.setType(paperForApp.getType());
            /* authors */
            List<Paper.Author> authors = new ArrayList<>();
            paperForApp.getAuthors().forEach(author -> {
                String researcherId = author.getId();
                Paper.Author paperAuthor = new Paper.Author();
                Researcher researcher;
                if (researcherId != null) {
                    researcher = Objects.requireNonNull(template.get(researcherId, Researcher.class));
                }
                else {
                    researcher = new Researcher();
                    researcher.setName(author.getName());
                    SearchHit<Institution> instHit = template.searchOne(
                            new NativeSearchQueryBuilder()
                                    .withQuery(QueryBuilders.termQuery("name.raw", author.getInstName()))
                                    .build(), Institution.class);
                    Researcher.Institution researcherInst = new Researcher.Institution();
                    Institution institution;
                    if (instHit != null) {
                        institution = instHit.getContent();
                    }
                    else {
                        institution = new Institution();
                        institution.setName(author.getInstName());
                        template.save(institution);
                        log.info("Added institution {}", institution.getId());
                    }
                    researcherInst.setId(institution.getId());
                    researcherInst.setName(author.getInstName());
                    researcher.setCurrentInst(researcherInst);
                    template.save(researcher);
                    log.info("Added researcher {}", researcher.getId());
                }
                paperAuthor.setId(researcher.getId());
                paperAuthor.setName(researcher.getName());
                authors.add(paperAuthor);
            });
            paper.setAuthors(authors);
            /* institutions */
            List<Paper.Institution> institutions = new ArrayList<>();
            paperForApp.getInstitutions().forEach(inst -> {
                String institutionId = inst.getId();
                Paper.Institution paperInst = new Paper.Institution();
                Institution institution;
                if (institutionId != null) {
                    institution = Objects.requireNonNull(template.get(institutionId, Institution.class));
                }
                else {
                    institution = new Institution();
                    institution.setName(inst.getName());
                    template.save(institution);
                    log.info("Added institution {}", institution.getId());
                }
                paperInst.setId(institution.getId());
                paperInst.setName(institution.getName());
                institutions.add(paperInst);
            });
            paper.setInstitutions(institutions);
            /* abstract */
            paper.setPaperAbstract(paperForApp.getPaperAbstract());
            /* keywords */
            paper.setKeywords(paperForApp.getKeywords());
            /* subjects */
            paper.setSubjects(paperForApp.getSubjects());
            /* year and date */
            Integer year = paperForApp.getYear();
            String date = paperForApp.getDate();
            if (year == null && date != null) {
                year = Integer.parseInt(date.substring(0, 4));
            }
            else if (year != null && date == null) {
                date = year + "-01-01";
            }
            paper.setYear(year);
            paper.setDate(date);
            /* doi */
            paper.setDoi(paperForApp.getDoi());
            /* journal */
            PaperForApp.Journal appJournal = paperForApp.getJournal();
            if (appJournal != null) {
                String journalId = appJournal.getId();
                Paper.Journal paperJournal = new Paper.Journal();
                Journal journal;
                if (journalId != null) {
                    journal = Objects.requireNonNull(template.get(journalId, Journal.class));
                }
                else {
                    journal = new Journal();
                    journal.setTitle(appJournal.getTitle());
                    template.save(journal);
                    log.info("Added journal {}", journal.getId());
                }
                paperJournal.setId(journal.getId());
                paperJournal.setTitle(journal.getTitle());
                paperJournal.setVolume(appJournal.getVolume());
                paperJournal.setIssue(appJournal.getIssue());
                paperJournal.setStartPage(appJournal.getStartPage());
                paperJournal.setEndPage(appJournal.getEndPage());
                paper.setJournal(paperJournal);
            }
            else {
                paper.setJournal(null);
            }
            /* publisher */
            paper.setPublisher(paperForApp.getPublisher());
            /* references */
            List<String> references = new ArrayList<>();
            List<PaperForApp.ReferencePaper> appReferences = paperForApp.getReferencePapers();
            if (appReferences == null) {
                appReferences = new ArrayList<>();
            }
            appReferences.forEach(ref -> {
                String paperId = ref.getId();
                if (paperId == null) {
                    Paper refPaper = new Paper();
                    refPaper.setTitle(ref.getTitle());
                    template.save(refPaper);
                    log.info("Added paper {}", refPaper.getId());
                    paperId = refPaper.getId();
                }
                references.add(paperId);
            });
            if (paper.getReferences() != null) {
                paper.getReferences().forEach(reference -> template.updateByQuery(UpdateQuery
                        .builder(reference)
                        .withScript("ctx._source.citationNum--")
                        .build(), IndexCoordinates.of("paper")));
            }
            references.forEach(reference -> template.updateByQuery(UpdateQuery
                    .builder(reference)
                    .withScript("ctx._source.citationNum++")
                    .build(), IndexCoordinates.of("paper")));
            paper.setReferences(references);
            template.save(paper);
            log.info("Successfully {} paper {}", paperIdIfExists == null ? "added" : "edited", paper.getId());
        }, "save-paper").start();
    }

    @Override
    public void removePaper(String paperId) {
        new Thread(() -> {
            Paper paper = template.get(paperId, Paper.class);
            if (paper == null)
                return;
            Trash trash = new Trash();
            trash.setTitle(paper.getTitle());
            List<String> authors = new ArrayList<>();
            if (paper.getAuthors() != null) {
                paper.getAuthors().forEach(author -> authors.add(author.getName()));
                trash.setAuthors(authors);
            }
            template.save(trash);
            template.delete(paperId, Paper.class);
            log.info("Successfully removed paper {}", paperId);
        }, "remove-paper").start();
    }

    @Override
    public void modifyResearcher(String researcherId, PortalForApp portal) {
        new Thread(() -> {
            Researcher researcher = template.get(researcherId, Researcher.class);
            Objects.requireNonNull(researcher);
            /* name */
            researcher.setName(portal.getName());
            /* currentInst */
            PortalForApp.Institution portalCurInst = portal.getCurrentInst();
            String curInstId = portalCurInst.getId();
            if (curInstId != null) {
                Institution curInst = template.get(curInstId, Institution.class);
                Objects.requireNonNull(curInst);
                Researcher.Institution researcherCurInst = new Researcher.Institution();
                researcherCurInst.setId(curInstId);
                researcherCurInst.setName(curInst.getName());
                researcher.setCurrentInst(researcherCurInst);
            }
            else {
                Institution curInst = new Institution();
                curInst.setName(portalCurInst.getName());
                template.save(curInst);
                log.info("Added institution {}", curInst.getId());
                Researcher.Institution researcherCurInst = new Researcher.Institution();
                researcherCurInst.setId(curInst.getId());
                researcherCurInst.setName(portalCurInst.getName());
                researcher.setCurrentInst(researcherCurInst);
            }
            /* institutions */
            if (portal.getInstitutions() != null) {
                List<Researcher.Institution> institutions = new ArrayList<>();
                portal.getInstitutions().forEach(portalInst -> {
                    String institutionId = portalInst.getId();
                    if (institutionId != null) {
                        Institution institution = template.get(institutionId, Institution.class);
                        Objects.requireNonNull(institution);
                        Researcher.Institution researcherInst = new Researcher.Institution();
                        researcherInst.setId(institutionId);
                        researcherInst.setName(institution.getName());
                        institutions.add(researcherInst);
                    }
                    else {
                        Institution institution = new Institution();
                        institution.setName(portalInst.getName());
                        template.save(institution);
                        log.info("Added institution {}", institution.getId());
                        Researcher.Institution researcherInst = new Researcher.Institution();
                        researcherInst.setId(institution.getId());
                        researcherInst.setName(portalInst.getName());
                        institutions.add(researcherInst);
                    }
                });
                researcher.setInstitutions(institutions);
            }
            else {
                researcher.setInstitutions(null);
            }
            /* hIndex, gIndex, interests */
            researcher.setHIndex(portal.getHIndex());
            researcher.setGIndex(portal.getGIndex());
            researcher.setInterests(portal.getInterests());
            template.save(researcher);
            log.info("Successfully modified researcher {}", researcher.getId());
        }, "modify-portal").start();
    }

    @Override
    public void transferPatent(String patentId, Transfer transfer) {
        new Thread(() -> {
            Patent patent = Objects.requireNonNull(template.get(patentId, Patent.class));
            patent.setApplicant(transfer.getApplicant());
            patent.setAddress(transfer.getAddress());
            patent.setAgency(transfer.getAgency());
            patent.setAgent(transfer.getAgent());
            template.save(patent);
            log.info("Successfully transferred patent {}", patent.getId());
        }, "transfer-patent").start();
    }

}
