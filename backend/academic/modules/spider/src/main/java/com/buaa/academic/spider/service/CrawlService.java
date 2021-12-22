package com.buaa.academic.spider.service;

public interface CrawlService {

    void crawlWithTitle(String title, String userId);

    void crawlWithUrl(String url, String userId);

}
