package com.buaa.academic.analysis.dao;

import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;

public interface CustomRepository {

    Subject findSubjectByName(String name);

    Topic findTopicByName(String name);

}
