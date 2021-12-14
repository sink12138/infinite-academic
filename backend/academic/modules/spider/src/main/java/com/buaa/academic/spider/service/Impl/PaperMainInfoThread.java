package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.model.queueObject.PaperObject;
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
public class PaperMainInfoThread implements Runnable{
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Start crawl paper main info...");
        log.info("{} started", threadName);

        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningMainInfoThreadNum ++;
        }

        PaperParser paperParser = new PaperParser();
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
                }
                else if (loop == 0) {
                    driver.quit();
                    service.stop();
                    service = ParserUtil.getDriverService();
                    service.start();
                    driver = ParserUtil.getDriver(headless);
                    paperParser.setDriver(driver);
                }

                if (StatusCtrl.jobStopped) {
                    if (driver != null)
                        driver.quit();
                    if (service != null)
                        service.stop();
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    log.info("{} stopped", threadName);
                    return;
                }

                PaperObject paperObject;
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.paperObjectQueue.size() == 0 && StatusCtrl.runningQueueInitThreadNum == 0) {
                        driver.quit();
                        service.stop();
                        StatusCtrl.runningMainInfoThreadNum--;
                        statusCtrl.changeRunningStatusStop(threadName, "Finished");
                        log.info("{} finished", threadName);
                        return;
                    }
                }
                paperObject = StatusCtrl.paperObjectQueue.poll();
                if (paperObject == null) {
                    Thread.sleep(2000);
                    continue;
                }
                paperParser.setPaperCraw(paperObject);
                paperParser.wanFangSpider();
                synchronized (StatusCtrl.queueLock) {
                    paperParser.getPaperCraw().setUrl("https://kns.cnki.net/kns8/defaultresult/index");
                    StatusCtrl.subjectAndTopicCrawlerQueue.add(paperParser.getPaperCraw());
                }

            } catch (Exception e) {
                statusCtrl.changeRunningStatusTo(threadName, Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                StatusCtrl.errorHandler.report();
            }
        }
    }
}
