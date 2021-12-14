package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.util.JournalParser;
import com.buaa.academic.spider.util.PaperParser;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SubjectTopicThread implements Runnable {

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Subject crawler start...");
        log.info("{} started", threadName);
        PaperParser paperParser = new PaperParser();
        JournalParser journalParser = new JournalParser();
        journalParser.setStatusCtrl(statusCtrl);
        journalParser.setHeadless(headless);
        paperParser.setJournalParser(journalParser);
        paperParser.setStatusCtrl(statusCtrl);

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        int period = 500;
        for (int loop = 0; ; loop = (loop + 1) % period) {
            try {
                if (service == null || driver == null) {
                    service = ParserUtil.getDriverService();
                    driver = ParserUtil.getDriver(headless);
                    paperParser.setDriver(driver);
                } else if (loop == 0) {
                    driver.quit();
                    service.stop();
                    service = ParserUtil.getDriverService();
                    service.start();
                    driver = ParserUtil.getDriver(headless);
                    paperParser.setDriver(driver);
                }

                if (StatusCtrl.jobStopped) {
                    driver.quit();
                    service.stop();
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    log.info("{} stopped", threadName);
                    return;
                }
                PaperObject paperObject;
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.subjectAndTopicCrawlerQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        driver.quit();
                        service.stop();
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        log.info("{} finished", threadName);
                        return;
                    }
                }
                paperObject = StatusCtrl.subjectAndTopicCrawlerQueue.poll();
                if (paperObject == null) {
                    Thread.sleep(2000);
                    continue;
                }
                paperParser.setPaperCraw(paperObject);
                paperParser.zhiWangSpider();

            } catch (Exception e) {
                statusCtrl.changeRunningStatusTo(threadName, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                StatusCtrl.errorHandler.report();
            }
        }

    }
}