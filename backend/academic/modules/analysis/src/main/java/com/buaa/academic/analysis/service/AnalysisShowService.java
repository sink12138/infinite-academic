package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.SearchAggregation;

import javax.servlet.http.HttpSession;

public interface AnalysisShowService {
    SearchAggregation searchAggregating(HttpSession session);
}
