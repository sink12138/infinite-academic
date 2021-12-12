package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.util.SearchParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerQueueInitThread implements Runnable {

    private String keyword;

    private StatusCtrl statusCtrl;

    private Boolean headless;

    @SneakyThrows
    @Override
    public void run() {
        statusCtrl.changeRunningStatusTo(Thread.currentThread().getName(), "Crawler queue init start...");

        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningQueueInitThreadNum ++;
        }
        String url = "https://s.wanfangdata.com.cn/periodical?q=" + keyword + "&style=table&s=50";
        SearchParser searchParser = new SearchParser();
        searchParser.setHeadless(headless);
        searchParser.setStatusCtrl(statusCtrl);
        searchParser.setUrl(url);
        searchParser.wanFangSpider();
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningQueueInitThreadNum --;
        }
        if (StatusCtrl.jobStopped) {
            statusCtrl.changeRunningStatusStop(Thread.currentThread().getName(), "Stopped");
        } else {
            statusCtrl.changeRunningStatusStop(Thread.currentThread().getName(), "Crawler queue init finished");
        }
    }
}
