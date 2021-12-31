package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.model.queueObject.PatentObject;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.PatentParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Arrays;

@Data
@Slf4j
@AllArgsConstructor
public class PatentMainInfoThread implements Runnable{
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Start crawl patent main info...");
        log.info("{} started", threadName);

        PatentParser patentParser = new PatentParser();
        patentParser.setStatusCtrl(statusCtrl);

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
                    patentParser.setDriver(driver);
                }
                else if (loop == 0) {
                    driver.quit();
                    service.stop();
                    service = ParserUtil.getDriverService();
                    service.start();
                    driver = ParserUtil.getDriver(headless);
                    patentParser.setDriver(driver);
                }

                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.patentObjectQueue.size() == 0 && StatusCtrl.runningPatentsInitThreadNum == 0) {
                        driver.quit();
                        service.stop();
                        statusCtrl.changeRunningStatusStop(threadName, "Finished");
                        log.info("{} finished", threadName);
                        return;
                    }
                }
                PatentObject patentObject = StatusCtrl.patentObjectQueue.poll();
                if (patentObject == null) {
                    Thread.sleep(2000);
                    continue;
                }
                patentParser.wanFangSpider(patentObject);

            } catch (Exception e) {
                statusCtrl.changeRunningStatusTo(threadName, Arrays.toString(e.getStackTrace()));
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
                    patentParser.setDriver(driver);
                } catch (Exception ignored) {}
            }
        }
    }
}
