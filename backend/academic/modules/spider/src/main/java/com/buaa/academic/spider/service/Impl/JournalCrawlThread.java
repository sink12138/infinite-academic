package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.JournalObject;
import com.buaa.academic.spider.util.JournalParser;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalCrawlThread implements Runnable{
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Journal crawler start.");
        ChromeDriverService service = null;
        RemoteWebDriver driver = null;
        while (true) {
            try {
                service = ParserUtil.getDriverService();
                service.start();
                driver = ParserUtil.getDriver(headless);

                if (StatusCtrl.jobStopped) {
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    driver.quit();
                    service.stop();
                    return;
                }
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.journalUrls.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        driver.quit();
                        service.stop();
                        return;
                    }
                }
                JournalObject journal = StatusCtrl.journalUrls.poll();
                if (journal == null) {
                    driver.quit();
                    service.stop();
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

                driver.quit();
                service.stop();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    assert driver != null;
                    driver.quit();
                    service.stop();
                } catch (Exception ignored) {}
            }
        }
    }
}
