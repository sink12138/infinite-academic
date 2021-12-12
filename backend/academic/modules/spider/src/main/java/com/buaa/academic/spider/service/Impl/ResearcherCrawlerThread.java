package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import com.buaa.academic.spider.util.ResearcherParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResearcherCrawlerThread implements Runnable{

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Researcher crawler start.");
        while (true) {
            try {
                if (StatusCtrl.jobStopped) {
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    return;
                }
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.researcherQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        statusCtrl.changeRunningStatusStop(threadName,  "Finished.");
                        return;
                    }
                }
                ResearcherSet researcherSet = StatusCtrl.researcherQueue.poll();
                if (researcherSet == null)
                    continue;
                String paperId = researcherSet.getPaperId();
                ArrayList<Paper.Author> authors = new ArrayList<>();
                for (ResearcherSet.ResearcherObject researcherObject : researcherSet.getResearcherObjects()) {
                    ResearcherParser researcherParser = new ResearcherParser();
                    researcherParser.setHeadless(headless);
                    researcherParser.setStatusCtrl(this.statusCtrl);
                    researcherParser.setUrl(researcherObject.getUrl());
                    researcherParser.wanFangSpider();
                    authors.add(new Paper.Author(researcherParser.getResearcher().getId(), researcherObject.getName()));
                }
                Paper paper = statusCtrl.existenceService.findPaperById(paperId);
                paper.setAuthors(authors);
                statusCtrl.template.save(paper);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
