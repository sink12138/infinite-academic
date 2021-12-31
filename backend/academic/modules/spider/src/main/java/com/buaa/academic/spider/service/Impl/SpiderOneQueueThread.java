package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.util.SearchParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class SpiderOneQueueThread implements Runnable{
    private StatusCtrl statusCtrl;
    private boolean headless;

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Crawler queue init start...");
        log.info("{} started", threadName);
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningPapersInitThreadNum++;
        }
        SearchParser searchParser = new SearchParser();
        searchParser.setHeadless(headless);
        searchParser.setStatusCtrl(statusCtrl);
        try {
            searchParser.wanfangSpiderOne();
        } catch (Exception e) {
            StatusCtrl.errorHandler.report(e);
        }
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningPapersInitThreadNum--;
        }
        if (StatusCtrl.jobStopped) {
            statusCtrl.changeRunningStatusStop(Thread.currentThread().getName(), "Stopped");
            log.info("{} stopped", threadName);
        } else {
            statusCtrl.changeRunningStatusStop(Thread.currentThread().getName(), "Crawler queue init finished");
            log.info("{} finished", threadName);
        }
    }
}
