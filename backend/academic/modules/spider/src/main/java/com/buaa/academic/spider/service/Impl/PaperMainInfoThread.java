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
public class PaperMainInfoThread implements Runnable{

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Start crawl paper main info...");

        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningMainInfoThreadNum ++;
        }

        PaperParser paperParser = new PaperParser();
        JournalParser journalParser = new JournalParser();
        journalParser.setStatusCtrl(statusCtrl);
        journalParser.setHeadless(headless);
        paperParser.setStatusCtrl(statusCtrl);
        paperParser.setJournalParser(journalParser);

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
                    if (StatusCtrl.paperObjectQueue.size() == 0 && StatusCtrl.runningQueueInitThreadNum == 0) {
                        StatusCtrl.runningMainInfoThreadNum--;
                        statusCtrl.changeRunningStatusStop(threadName, "Finished");
                        driver.quit();
                        service.stop();
                        return;
                    }
                }
                paperObject = StatusCtrl.paperObjectQueue.poll();
                if (paperObject == null) {
                    driver.quit();
                    service.stop();
                    continue;
                }
                paperParser.setPaperCraw(paperObject);
                paperParser.wanFangSpider();
                synchronized (StatusCtrl.queueLock) {
                    paperParser.getPaperCraw().setUrl("https://kns.cnki.net/kns8/defaultresult/index");
                    StatusCtrl.subjectAndTopicCrawlerQueue.add(paperParser.getPaperCraw());
                }

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
