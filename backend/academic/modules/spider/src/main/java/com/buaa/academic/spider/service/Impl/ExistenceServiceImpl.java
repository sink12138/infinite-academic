package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.spider.service.ExistenceService;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExistenceServiceImpl implements ExistenceService {
    @Autowired
    ElasticsearchRestTemplate template;

    @Override
    public Paper findPaperByTileAndAuthors(String title, List<Paper.Author> authors) {
        List<String> authorsNames = new ArrayList<>();
        authors.forEach(author -> authorsNames.add(author.getName()));
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery().must(
                        QueryBuilders.termQuery("title.raw", title)
                ).must(QueryBuilders.termsQuery("authors.name", authorsNames.toArray())))
                .build();
        SearchHit<Paper> paper = template.searchOne(query, Paper.class);
        if (paper == null)
            return null;
        return paper.getContent();
    }

    @Override
    public Paper findPaperById(String id) {
        return template.get(id, Paper.class);
    }

    @Override
    public Researcher findResearcherById(String id) {
        return template.get(id, Researcher.class);
    }

    @Override
    public Institution findInstByName(String name) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("name,raw", name))
                .build();
        SearchHit<Institution> searchHit = template.searchOne(query, Institution.class);
        if (searchHit == null)
            return null;
        return searchHit.getContent();
    }

    @Override
    public Journal findJournalByName(String name) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("title,raw", name))
                .build();
        SearchHit<Journal> searchHit = template.searchOne(query, Journal.class);
        if (searchHit == null)
            return null;
        return searchHit.getContent();
    }
}
