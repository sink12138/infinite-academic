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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PaperSourceThread implements Runnable {
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Paper source crawler start.");
        log.info("{} started", threadName);

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        int period = 500;
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
                    if (StatusCtrl.sourceQueue.size() == 0 && StatusCtrl.runningPapersInitThreadNum == 0) {
                        if (driver != null) {
                            driver.quit();
                            service.stop();
                        }
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        log.info("{} finished", threadName);
                        return;
                    }
                }

                PaperObject paperObject = StatusCtrl.sourceQueue.poll();
                if (paperObject == null) {
                    Thread.sleep(2000);
                    continue;
                }

                PaperParser paperParser = new PaperParser();
                paperParser.setPaperCrawl(paperObject);
                paperParser.setDriver(driver);
                paperParser.setStatusCtrl(statusCtrl);
                paperParser.baiduSpider();

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
