package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.document.statistic.DataPerYear;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

public interface AnalysisShowService {
    SearchAggregation searchAggregating(HttpSession session);
    ArrayList<SimpleTopic> getHotTopics();
    ArrayList<SimpleSubject> getHotSubjects();
    ArrayList<DataPerYear> getPublicationStatic(String type, String id);
    Boolean checkTargetExist(String type, String id);
    ArrayList<WordFrequency> topicFrequencyStatic(String type, String id);
    ArrayList<Cooperation> getCooperativeRelations(String type, String id);
}
