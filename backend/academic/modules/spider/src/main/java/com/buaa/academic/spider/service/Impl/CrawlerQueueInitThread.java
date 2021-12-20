package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.util.SearchParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CrawlerQueueInitThread implements Runnable {

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        statusCtrl.changeRunningStatusTo(threadName, "Crawler queue init start...");
        log.info("{} started", threadName);
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningQueueInitThreadNum ++;
        }
        while (StatusCtrl.keywordQueue.size() != 0) {
            if (StatusCtrl.jobStopped)
                break;
            String keyword = StatusCtrl.keywordQueue.poll();
            log.info("{} polled new keyword", threadName);
            // String url = "https://s.wanfangdata.com.cn/paper?q=" + keyword + "&style=table&s=50";
            String url = "https://s.wanfangdata.com.cn/paper?q=" + keyword + "&style=detail&s=50";
            SearchParser searchParser = new SearchParser();
            searchParser.setHeadless(headless);
            searchParser.setStatusCtrl(statusCtrl);
            searchParser.setUrl(url);
            try {
                searchParser.wanFangSpider();
            } catch (Exception e) {
                StatusCtrl.errorHandler.report(e);
            }
        }
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningQueueInitThreadNum --;
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
