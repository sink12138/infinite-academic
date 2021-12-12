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
public class SubjectTopicThread implements Runnable{

    private StatusCtrl statusCtrl;

    @SneakyThrows
    @Override
    public void run() {
        PaperParser paperParser = new PaperParser();
        JournalParser journalParser = new JournalParser();
        journalParser.setStatusCtrl(statusCtrl);
        paperParser.setJournalParser(journalParser);
        paperParser.setStatusCtrl(statusCtrl);
        while (true) {
            try {
                if (StatusCtrl.jobStopped)
                    return;
                PaperObject paperObject;
                synchronized (StatusCtrl.queueLock) {
                    if (StatusCtrl.subjectAndTopicCrawlerQueue.size() == 0 && StatusCtrl.runningMainInfoThreadNum == 0) {
                        return;
                    }
                }
                paperObject = StatusCtrl.subjectAndTopicCrawlerQueue.poll();
                if (paperObject == null)
                    continue;
                paperParser.setPaperCraw(paperObject);
                System.out.println("正在获取论文学科话题信息：" + paperObject.getPaper().getTitle());
                paperParser.zhiWangSpider();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}