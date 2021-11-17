package com.buaa.academic.search.service.impl;

import com.buaa.academic.document.entity.Reducible;
import com.buaa.academic.search.model.vo.Relations;
import com.buaa.academic.search.service.RelationService;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationServiceImpl implements RelationService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public <I, T extends Reducible<I>> Relations<I> searchRelations(Class<T> target, String id, String field, int page) {
        Relations<I> relations = new Relations<>();
        SearchHits<T> hits = template.search(
                new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termQuery(field, id))
                        .withPageable(PageRequest.of(page, 10))
                        .withTrackTotalHits(true)
                        .build(),
                target);
        hits.forEach(hit -> relations.getRelations().add(hit.getContent().reduce()));
        int totalPages  = (int) ((hits.getTotalHits() + 9) / 10);
        relations.setPage(Math.min(page, totalPages - 1));
        relations.setTotalPages(totalPages);
        return relations;
    }

    @Override
    public <I, T extends Reducible<I>> Relations<I> searchReferences(Class<T> target, List<String> references, int page) {
        Relations<I> relations = new Relations<>();
        String[] terms = references.toArray(new String[0]);
        SearchHits<T> hits = template.search(
                new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.termsQuery("_id", terms))
                        .withPageable(PageRequest.of(page, 10))
                        .withTrackTotalHits(true)
                        .build(),
                target);
        hits.forEach(hit -> relations.getRelations().add(hit.getContent().reduce()));
        int totalPages  = (int) ((hits.getTotalHits() + 9) / 10);
        relations.setPage(Math.min(page, totalPages - 1));
        relations.setTotalPages(totalPages);
        return relations;
    }

}
