package com.buaa.academic.spider.controller;

import com.buaa.academic.spider.model.queueObject.PaperObject;
import com.buaa.academic.spider.model.queueObject.ResearcherObject;
import com.buaa.academic.spider.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SpiderController {

    //测试
    @GetMapping("/spiderInstitutionTest")
    public void spider(@RequestParam String keyword) throws InterruptedException {
        //todo 多线程

        List<PaperObject> paperCraw = new ArrayList<>();
        List<PaperObject> subjectAndTopic = new ArrayList<>();
        List<ResearcherObject> researcherInterest = new ArrayList<>();

        String url = "https://s.wanfangdata.com.cn/periodical?q=" + keyword + "&style=table&s=50";
        SearchParser searchParser = new SearchParser();
        searchParser.setUrl(url);
        searchParser.wanFangSpider();

        paperCraw.addAll(searchParser.getRootPaperList());

        int total = 0;

        //万方获取主要数据
        while (paperCraw.size() != 0 && total <= 10) {
            PaperParser paperParser = new PaperParser();
            PaperObject paperObject = paperCraw.remove(0);
            System.out.println("Current Url is: " + paperObject.getUrl());
            paperParser.setPaperCraw(paperObject);
            paperParser.wanFangSpider();
            total++;
            System.out.println("finish " + total + " : " + paperParser.getPaperCraw().getUrl());
            paperCraw.addAll(paperParser.getToCrawPaperList());
            paperParser.getPaperCraw().setUrl("https://kns.cnki.net/kns8/defaultresult/index");
            subjectAndTopic.add(paperParser.getPaperCraw());
        }
        //知网获取论文学科话题
        while (subjectAndTopic.size() != 0) {
            PaperParser paperParser = new PaperParser();
            PaperObject paperObject = subjectAndTopic.remove(0);
            paperParser.setPaperCraw(paperObject);
            System.out.println("正在获取论文学科话题信息：" + paperObject.getPaper().getTitle());
            paperParser.zhiWangSpider();
        }
    }
}
