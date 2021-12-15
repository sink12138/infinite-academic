package com.buaa.academic.search.service.impl;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Reducible;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.search.model.vo.Relations;
import com.buaa.academic.search.service.RelationService;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        hits.forEach(hit -> relations.getItems().add(hit.getContent().reduce()));
        int totalPages  = (int) ((hits.getTotalHits() + 9) / 10);
        relations.setPage(Math.min(page, totalPages - 1));
        relations.setTotalPages(totalPages);
        return relations;
    }

    @Override
    public Relations<PaperItem> searchReferences(List<String> references, int page) {
        Relations<PaperItem> relations = new Relations<>();
        if (references == null)
            references = Collections.emptyList();
        int totalPages  = Math.max(1, (references.size() + 9) / 10);
        relations.setTotalPages(totalPages);
        relations.setPage(Math.min(page, totalPages - 1));
        if (page > totalPages || references.isEmpty()) {
            return relations;
        }
        List<String> refSubList = references.subList(page * 10, (page + 1) * 10);
        List<PaperItem> items = new ArrayList<>();
        refSubList.forEach(ref -> {
            if (ref.matches("^[0-9A-Za-z_-]{20}$")) {
                items.add(Objects.requireNonNull(template.get(ref, Paper.class)).reduce());
            }
            else {
                PaperItem raw = new PaperItem();
                raw.setRaw(ref);
                items.add(raw);
            }
        });
        relations.setItems(items);
        return relations;
    }

}
