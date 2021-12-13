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
        RemoteWebDriver driver = ParserUtil.getDriver(headless);
        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Subject crawler start...");
        PaperParser paperParser = new PaperParser();
        JournalParser journalParser = new JournalParser();
        journalParser.setStatusCtrl(statusCtrl);
        journalParser.setHeadless(headless);
        paperParser.setJournalParser(journalParser);
        paperParser.setStatusCtrl(statusCtrl);
        paperParser.setDriver(driver);
        while (true) {
            try {
                if (StatusCtrl.jobStopped) {
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    driver.quit();
                    return;
                }
                PaperObject paperObject;
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.subjectAndTopicCrawlerQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        statusCtrl.changeRunningStatusStop(threadName,  "Finished.");
                        driver.quit();
                        return;
                    }
                }
                paperObject = StatusCtrl.subjectAndTopicCrawlerQueue.poll();
                if (paperObject == null)
                    continue;
                paperParser.setPaperCraw(paperObject);
                paperParser.zhiWangSpider();
            } catch (Exception e) {
                statusCtrl.changeRunningStatusTo(threadName, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
            }
        }

    }
}
