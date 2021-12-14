package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.JournalObject;
import com.buaa.academic.spider.util.JournalParser;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class JournalCrawlThread implements Runnable{
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Journal crawler start.");
        log.info("{} started", threadName);
        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        int period = 500;
        for (int loop = 0; ; loop = (loop + 1) % period) {
            try {
                if (service == null || driver == null) {
                    service = ParserUtil.getDriverService();
                    driver = ParserUtil.getDriver(headless);
                }
                else if (loop == 0) {
                    driver.quit();
                    service.stop();
                    service = ParserUtil.getDriverService();
                    service.start();
                    driver = ParserUtil.getDriver(headless);
                }

                if (StatusCtrl.jobStopped) {
                    driver.quit();
                    service.stop();
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    log.info("{} stopped", threadName);
                    return;
                }
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.journalUrls.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        driver.quit();
                        service.stop();
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        log.info("{} finished", threadName);
                        return;
                    }
                }
                JournalObject journal = StatusCtrl.journalUrls.poll();
                if (journal == null) {
                    Thread.sleep(2000);
                    continue;
                }

                JournalParser journalParser = new JournalParser();
                journalParser.setDriver(driver);
                journalParser.setUrl(journal.getJournalUrl());
                journalParser.setStatusCtrl(statusCtrl);
                journalParser.wanFangSpider();

                String journalId = journalParser.getJournal().getId();
                Paper paper = statusCtrl.template.get(journal.getPaperId(), Paper.class);
                assert paper != null;
                paper.getJournal().setId(journalId);
                statusCtrl.template.save(paper);

            } catch (Exception e) {
                e.printStackTrace();
                StatusCtrl.errorHandler.report();
            }
        }
    }
}
