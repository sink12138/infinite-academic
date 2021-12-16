package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.spider.util.ParserUtil;
import com.buaa.academic.spider.util.ResearcherParser;
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
public class InterestsGIndexThread implements Runnable {

    private StatusCtrl statusCtrl;

    private boolean headless;

    @SneakyThrows
    @Override
    public void run() {

        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Interests crawler started.");
        log.info("{} started", threadName);

        ChromeDriverService service = null;
        RemoteWebDriver driver = null;
        do {
            if (StatusCtrl.jobStopped) {
                statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                log.info("{} stopped", threadName);
                return;
            }
            try {
                service = ParserUtil.getDriverService();
                service.start();
                driver = ParserUtil.getDriver(headless);
                driver.get("https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page");
            }
            catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
            }
        } while (service == null || driver == null);

        ResearcherParser researcherParser = new ResearcherParser();
        researcherParser.setHeadless(headless);
        researcherParser.setStatusCtrl(statusCtrl);

        while (true) {
            if (StatusCtrl.jobStopped) {
                driver.quit();
                service.stop();
                statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                log.info("{} stopped", threadName);
                return;
            }

            try {
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.interestsQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        driver.quit();
                        service.stop();
                        statusCtrl.changeRunningStatusStop(threadName, "Finished.");
                        log.info("{} finished", threadName);
                        return;
                    }
                }
                Researcher researcher = StatusCtrl.interestsQueue.poll();
                if (researcher == null) {
                    Thread.sleep(2000);
                    continue;
                }

                researcherParser.setResearcher(researcher);
                researcherParser.setDriver(driver);
                researcherParser.setBanned(false);
                researcherParser.baiDuSpider();

                if (researcherParser.isBanned()) {
                    statusCtrl.changeRunningStatusTo(threadName, "Banned, sleeping...");
                }
            }
            catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
                do {
                    if (StatusCtrl.jobStopped) {
                        driver.quit();
                        service.stop();
                        statusCtrl.changeRunningStatusStop(threadName, "Stopped.");
                        log.info("{} stopped", threadName);
                        return;
                    }
                    try {
                        driver.quit();
                        service.stop();
                        service = ParserUtil.getDriverService();
                        service.start();
                        driver = ParserUtil.getDriver(headless);
                        driver.get("https://xueshu.baidu.com/usercenter/data/authorchannel?cmd=inject_page");
                    }
                    catch (Exception ex) {
                        StatusCtrl.errorHandler.report(ex);
                    }
                } while (true);
            }
        }
    }

}
