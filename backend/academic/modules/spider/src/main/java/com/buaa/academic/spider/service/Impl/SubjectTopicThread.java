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
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectTopicThread implements Runnable{

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Subject crawler start...");
        PaperParser paperParser = new PaperParser();
        JournalParser journalParser = new JournalParser();
        journalParser.setStatusCtrl(statusCtrl);
        journalParser.setHeadless(headless);
        paperParser.setJournalParser(journalParser);
        paperParser.setStatusCtrl(statusCtrl);

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        while (true) {
            try {
                service = ParserUtil.getDriverService();
                service.start();
                driver = ParserUtil.getDriver(headless);
                paperParser.setDriver(driver);

                if (StatusCtrl.jobStopped) {
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    driver.quit();
                    service.stop();
                    return;
                }
                PaperObject paperObject;
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.subjectAndTopicCrawlerQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        statusCtrl.changeRunningStatusStop(threadName,  "Finished.");
                        driver.quit();
                        service.stop();
                        return;
                    }
                }
                paperObject = StatusCtrl.subjectAndTopicCrawlerQueue.poll();
                if (paperObject == null) {
                    driver.quit();
                    service.stop();
                    continue;
                }
                paperParser.setPaperCraw(paperObject);
                paperParser.zhiWangSpider();

                driver.quit();
                service.stop();
            } catch (Exception e) {
                statusCtrl.changeRunningStatusTo(threadName, Arrays.toString(e.getStackTrace()));
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
