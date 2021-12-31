package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.EntityBucket;
import com.buaa.academic.analysis.model.Bucket;
import com.buaa.academic.analysis.model.HotWord;
import com.buaa.academic.document.statistic.NumPerYear;
import org.elasticsearch.index.query.WrapperQueryBuilder;

import java.util.List;
import java.util.Map;

public interface AnalysisShowService {

    Map<String, List<Bucket>> searchAggregate(String indexName, WrapperQueryBuilder query, WrapperQueryBuilder filter);

    List<HotWord> getHotWords(String field, int num);

    NumPerYear getPublicationStatic(String type, String id);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean existsTarget(Class<?> target, String id);

    List<Bucket> wordFrequencyStatistics(String type, String id, String field, int num);

    List<EntityBucket> getCooperativeRelations(String type, String id, int num);

    List<EntityBucket> topEntities(String field, String name, String type, int num);

}
