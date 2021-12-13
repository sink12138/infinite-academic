package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.util.PaperParser;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperSourceThread implements Runnable {
    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Paper source crawler start.");

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;

        int period = 500;
        for (int loop = 0; ; loop = (loop + 1) % period) {
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
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        if (driver != null) {
                            driver.quit();
                            service.stop();
                        }
                        return;
                    }
                }

                PaperObject paperObject = StatusCtrl.sourceQueue.poll();
                if (paperObject == null) {
/*
                    if (driver != null) {
                        driver.quit();
                        service.stop();
                    }
*/
                    Thread.sleep(2000);
                    continue;
                }

                PaperParser paperParser = new PaperParser();
                paperParser.setPaperCraw(paperObject);
                paperParser.setDriver(driver);
                paperParser.setStatusCtrl(statusCtrl);
                paperParser.baiduSpider();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
