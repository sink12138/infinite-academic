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

    @SneakyThrows
    @Override
    public void run() {
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningQueueInitThreadNum ++;
        }
        String url = "https://s.wanfangdata.com.cn/periodical?q=" + keyword + "&style=table&s=50";
        SearchParser searchParser = new SearchParser();
        searchParser.setStatusCtrl(statusCtrl);
        searchParser.setUrl(url);
        searchParser.wanFangSpider();
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningQueueInitThreadNum --;
        }
    }
}
