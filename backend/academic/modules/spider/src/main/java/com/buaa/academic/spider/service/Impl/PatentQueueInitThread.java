package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.util.SearchParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
public class PatentQueueInitThread implements Runnable{

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Crawler queue init start...");
        log.info("{} started", threadName);
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningPatentsInitThreadNum++;
        }
        while (StatusCtrl.patentKeywordQueue.size() != 0) {
            if (StatusCtrl.jobStopped)
                break;
            String keyword = StatusCtrl.patentKeywordQueue.poll();
            log.info("{} polled new keyword", threadName);
            String url = "https://s.wanfangdata.com.cn/patent?q=" + keyword + "&s=50&style=detail";
            SearchParser searchParser = new SearchParser();
            searchParser.setHeadless(headless);
            searchParser.setStatusCtrl(statusCtrl);
            searchParser.setUrl(url);
            try {
                searchParser.wanFangPatentSpider();
            } catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
            }
        }
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningPatentsInitThreadNum--;
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
