package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.util.JournalParser;
import com.buaa.academic.spider.util.PaperParser;
import com.buaa.academic.spider.util.StatusCtrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

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
            try {
                if (StatusCtrl.jobStopped)
                    return;

                PaperObject paperObject;
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.paperObjectQueue.size() == 0 && StatusCtrl.runningQueueInitThreadNum == 0) {
                        StatusCtrl.runningMainInfoThreadNum--;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
