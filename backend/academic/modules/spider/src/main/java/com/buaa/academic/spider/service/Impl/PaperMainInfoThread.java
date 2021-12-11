package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.util.JournalParser;
import com.buaa.academic.spider.util.PaperParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperMainInfoThread implements Runnable{

    private StatusCtrl statusCtrl;

    @SneakyThrows
    @Override
    public void run() {
        synchronized (StatusCtrl.queueLock) {
            StatusCtrl.runningMainInfoThreadNum ++;
        }

        PaperParser paperParser = new PaperParser();
        JournalParser journalParser = new JournalParser();
        journalParser.setStatusCtrl(statusCtrl);
        paperParser.setStatusCtrl(statusCtrl);
        paperParser.setJournalParser(journalParser);
        while (true) {
            PaperObject paperObject;
            synchronized (StatusCtrl.queueLock) {
                if (StatusCtrl.paperObjectQueue.size() == 0 && StatusCtrl.runningQueueInitThreadNum == 0) {
                    StatusCtrl.runningMainInfoThreadNum --;
                    return;
                }
            }
            paperObject = StatusCtrl.paperObjectQueue.poll();
            if (paperObject == null)
                continue;
            System.out.println("Current Url is: " + paperObject.getUrl());
            paperParser.setPaperCraw(paperObject);
            paperParser.wanFangSpider();
            synchronized (StatusCtrl.queueLock) {
                //StatusCtrl.paperObjectQueue.addAll(paperParser.getToCrawPaperList());
                paperParser.getPaperCraw().setUrl("https://kns.cnki.net/kns8/defaultresult/index");
                StatusCtrl.subjectAndTopicCrawlerQueue.add(paperParser.getPaperCraw());
            }
        }
    }
}
