package com.buaa.academic.analysis.service.impl.hot;

import com.buaa.academic.document.entity.Paper;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

public class HotUpdateMainThread implements Runnable{
    public static Integer total = 0;
    public static Integer finished = 0;
    public static Boolean stopped = false;
    public static String scrollId;

    private Integer jobsNum;
    private Integer partition;
    private String threadName;
    private ElasticsearchRestTemplate template;

    public HotUpdateMainThread(ElasticsearchRestTemplate template) {
        this.template = template;
    }

    public HotUpdateMainThread setJobsNum(Integer jobsNum) {
        this.jobsNum = jobsNum;
        return this;
    }

    public HotUpdateMainThread setThreadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    @Override
    public void run() {

    }


}
