package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.ResearcherParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ResearcherCrawlerThread implements Runnable {

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Researcher crawler start.");
        log.info("{} started", threadName);

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        int period = 100;
        for (int loop = 0; ; loop = (loop + 1) % period) {
            if (StatusCtrl.jobStopped) {
                if (driver != null)
                    driver.quit();
                if (service != null)
                    service.stop();
                statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                log.info("{} stopped", threadName);
                return;
            }

            try {
                if (service == null || driver == null) {
                    service = ParserUtil.getDriverService();
                    driver = ParserUtil.getDriver(headless);
                } else if (loop == 0) {
                    driver.quit();
                    service.stop();
                    service = ParserUtil.getDriverService();
                    service.start();
                    driver = ParserUtil.getDriver(headless);
                }

                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.researcherQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        if (driver != null) {
                            driver.quit();
                            assert service != null;
                            service.stop();
                        }
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        log.info("{} finished", threadName);
                        return;
                    }
                }
                ResearcherSet researcherSet = StatusCtrl.researcherQueue.poll();
                if (researcherSet == null) {
                    Thread.sleep(2000);
                    continue;
                }
                String paperId = researcherSet.getPaperId();
                ArrayList<Paper.Author> authors = new ArrayList<>();

                for (ResearcherSet.ResearcherObject researcherObject : researcherSet.getResearcherObjects()) {
                    if (researcherObject.getUrl() == null) {
                        authors.add(new Paper.Author(null, researcherObject.getName()));
                        continue;
                    }
                    ResearcherParser researcherParser = new ResearcherParser();
                    researcherParser.setDriver(driver);
                    researcherParser.setHeadless(headless);
                    researcherParser.setStatusCtrl(this.statusCtrl);
                    researcherParser.setUrl(researcherObject.getUrl());
                    researcherParser.wanFangSpider();
                    if (researcherParser.getResearcher() != null)
                        authors.add(new Paper.Author(researcherParser.getResearcher().getId(), researcherObject.getName()));
                    else
                        authors.add(new Paper.Author(null, researcherObject.getName()));
                }

                Paper paper = statusCtrl.esUtil.findPaperById(paperId);
                paper.setAuthors(authors);
                statusCtrl.template.save(paper);

            } catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
                try {
                    if (driver != null)
                        driver.quit();
                } catch (Exception ignored) {}
                try {
                    if (service != null)
                        service.stop();
                } catch (Exception ignored) {}
                try {
                    service = ParserUtil.getDriverService();
                    service.start();
                    driver = ParserUtil.getDriver(headless);
                } catch (Exception ignored) {}
            }
        }
    }
}
