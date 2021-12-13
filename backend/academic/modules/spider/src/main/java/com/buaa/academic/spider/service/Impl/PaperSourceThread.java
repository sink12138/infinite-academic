package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.model.queueObject.ResearcherSet;
import com.buaa.academic.spider.util.PaperParser;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.ResearcherParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperSourceThread implements Runnable{
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Paper source crawler start.");

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        while (true) {
            try {
                service = ParserUtil.getDriverService();
                service.start();
                driver = ParserUtil.getDriver(headless);

                if (StatusCtrl.jobStopped) {
                    statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                    if (driver != null) {
                        driver.quit();
                        service.stop();
                    }
                    return;
                }

                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.sourceQueue.size() == 0 && StatusCtrl.runningQueueInitThreadNum == 0) {
                        statusCtrl.changeRunningStatusStop(threadName,  "Finished.");
                        if (driver != null) {
                            driver.quit();
                            service.stop();
                        }
                        return;
                    }
                }

                PaperObject paperObject = StatusCtrl.sourceQueue.poll();
                if (paperObject == null) {
                    if (driver != null) {
                        driver.quit();
                        service.stop();
                    }
                    continue;
                }

                PaperParser paperParser = new PaperParser();
                paperParser.setPaperCraw(paperObject);
                paperParser.setDriver(driver);
                paperParser.setStatusCtrl(statusCtrl);
                paperParser.baiduSpider();

                if (driver != null) {
                    driver.quit();
                    service.stop();
                }

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
