package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.document.statistic.SumPerYear;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface AnalysisShowService {

    List<SearchAggregation> searchAggregating(HttpSession session);

    List<HotWord> getHotWords(String field, int num);

    SumPerYear getPublicationStatic(String type, String id);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean existsTarget(Class<?> target, String id);

    List<Frequency> wordFrequencyStatistics(String type, String id, String field, int num);

    List<EntityFrequency> getCooperativeRelations(String type, String id, int num);

    List<EntityFrequency> topEntities(String field, String name, String type, int num);

}
