package com.buaa.academic.spider.controller;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.spider.service.AminerService;
import com.buaa.academic.spider.util.EsUtil;
import com.buaa.academic.spider.util.StringUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

@RestController
public class AminerController {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private AminerService aminerService;

    @Autowired
    private EsUtil esUtil;

    @PostMapping("/aminer")
    public Result<Void> aminer() throws IOException {
        Result<Void> result = new Result<>();
        for (int i = 0; i < 3; ++i) {
            Scanner scanner = new Scanner(new File("/data/tmp/aminer/papers/aminer_papers_" + i + ".txt"));
            for (int j = 0; j < 3; ++j) {
                new Thread(new AminerLinker(scanner), "aminer-link-" + i + '-' + j).start();
            }
        }
        return result;
    }

    private class AminerLinker implements Runnable {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private final Scanner scanner;

        private int count = 0;

        public AminerLinker(Scanner scanner) {
            this.scanner = scanner;
        }

        @Override
        @SneakyThrows
        public void run() {
            while (true) {
                String line;
                synchronized (scanner) {
                    if (scanner.hasNext()) {
                        StringBuilder builder = new StringBuilder();
                        do {
                            line = scanner.nextLine();
                            builder.append(line);
                        } while (!line.endsWith("}") && scanner.hasNext());
                        line = builder.toString();
                    } else {
                        break;
                    }
                }
                try {
                    Paper paper;
                    try {
                        paper = JSONObject.parseObject(line, Paper.class);
                    } catch (JSONException exception) {
                        log.warn(exception.getMessage());
                        log.warn("JSON source: {}", line);
                        continue;
                    }
                    paper.setId(null);
                    paper.setCrawled(true);
                    // Authors
                    for (Paper.Author author : paper.getAuthors()) {
                        String[] terms = author.getName().split("@@");
                        String name = terms[0];
                        String instName = terms.length > 1 ? StringUtil.formatInstitutionName(terms[1]) : null;
                        author.setName(name);
                        Researcher researcher = null;
                        // Find if this author is already in elasticsearch
                        if (instName != null)
                            researcher = esUtil.findResearcherByNameAndInst(name, instName);
                        if (researcher != null) {
                            researcher.setPaperNum(researcher.getPaperNum() + 1);
                            template.save(researcher);
                            author.setId(researcher.getId());
                            continue;
                        }
                        // Find if this author is in aminer dataset
                        if (author.getId() != null) {
                            researcher = aminerService.findResearcher(author.getId());
                            author.setId(null);
                        }
                        // Found in dataset
                        if (researcher != null) {
                            researcher.setId(null);
                            if (researcher.getCurrentInst() == null && instName != null) {
                                Institution currentInst = aminerService.findInstitution(instName);
                                researcher.setCurrentInst(new Researcher.Institution(currentInst.getId(), instName));
                            }
                            for (Researcher.Institution researcherInst : researcher.getInstitutions()) {
                                Institution institution = aminerService.findInstitution(researcherInst.getName());
                                researcherInst.setId(institution.getId());
                            }
                            researcher.setPaperNum(1);
                            template.save(researcher);
                            author.setId(researcher.getId());
                            continue;
                        }
                        // Not found - create new
                        if (instName != null) {
                            researcher = new Researcher();
                            researcher.setName(name);
                            Institution institution = aminerService.findInstitution(instName);
                            researcher.setCurrentInst(new Researcher.Institution(institution.getId(), instName));
                            researcher.setPaperNum(1);
                            template.save(researcher);
                            author.setId(researcher.getId());
                        }
                    }
                    if (esUtil.inTrash(paper.getTitle(), paper.getAuthors()))
                        continue;
                    Paper exists = esUtil.findPaperByTileAndAuthors(paper.getTitle(), paper.getAuthors());
                    if (exists != null) {
                        if (exists.isCrawled())
                            continue;
                        else
                            paper.setId(exists.getId());
                    }
                    // Institutions
                    if (paper.getInstitutions() != null) {
                        for (Paper.Institution paperInst : paper.getInstitutions()) {
                            paperInst.setName(StringUtil.formatInstitutionName(paperInst.getName()));
                            Institution institution = aminerService.findInstitution(paperInst.getName());
                            paperInst.setId(institution.getId());
                        }
                    }
                    // Journal
                    if (paper.getJournal() != null) {
                        Paper.Journal paperJournal = paper.getJournal();
                        // Find if this journal is already in elasticsearch
                        Journal journal = esUtil.findJournalByName(paperJournal.getTitle());
                        if (journal != null) {
                            paperJournal.setId(journal.getId());
                        } else {
                            // Find if this journal is in aminer dataset
                            if (paperJournal.getId() != null) {
                                journal = aminerService.findJournal(paperJournal.getId());
                                paperJournal.setId(null);
                            }
                            // Found in dataset
                            if (journal != null) {
                                journal.setId(null);
                            } else {
                                // Not found - create
                                journal = new Journal();
                                journal.setTitle(paperJournal.getTitle());
                            }
                            template.save(journal);
                            paperJournal.setId(journal.getId());
                        }
                    }
                    // Sources
                    if (exists != null && exists.getSources() != null) {
                        if (paper.getSources() == null) {
                            paper.setSources(exists.getSources());
                        } else {
                            List<Paper.Source> sources = paper.getSources();
                            sources.addAll(0, exists.getSources());
                            for (int i = 0; i < sources.size(); ++i) {
                                for (int j = i + 1; j < paper.getSources().size(); ) {
                                    if (sources.get(i).getWebsite().equals(sources.get(j).getWebsite()))
                                        sources.remove(j);
                                    else
                                        ++j;
                                }
                            }
                        }
                    }
                    // Date and year
                    String date = paper.getDate();
                    if (date != null && !date.matches("^\\d{4}-\\d{2}-\\d{2}$") && paper.getYear() != null) {
                        paper.setDate(paper.getYear() + "-01-01");
                    }
                    template.save(paper);
                    ++count;
                }
                catch (Exception exception) {
                    log.warn("Unknown exception: {}", exception.getMessage());
                }
                if (count % 1000 == 0) {
                    log.info("Saved paper: {}", count);
                }
            }
            log.info("Finished - saved paper: {}", count);
        }

    }

}
