package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.document.statistic.DataByYear;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

public interface AnalysisShowService {
    SearchAggregation searchAggregating(HttpSession session);
    ArrayList<HotWord> getHotWords(String field);
    DataByYear getPublicationStatic(String type, String id);
    Boolean checkTargetExist(String type, String id);
    ArrayList<Frequency> wordFrequencyStatic(String type, String id, String field);
    ArrayList<EntityFrequency> getCooperativeRelations(String type, String id);
    ArrayList<EntityFrequency> topEntities(String field, String name, String type);
}
