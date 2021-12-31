package com.buaa.academic.search.service;

import com.buaa.academic.document.entity.Reducible;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.search.model.vo.Relations;

import java.util.List;

public interface RelationService {

    <I, T extends Reducible<I>> Relations<I> searchRelations(Class<T> target, String id, String field, int page, String sort);

    Relations<PaperItem> searchReferences(List<String> references, int page);

}
