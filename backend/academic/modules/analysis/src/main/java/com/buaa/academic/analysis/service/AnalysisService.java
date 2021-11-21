package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.Status;

public interface AnalysisService {
    boolean associationAnalysis();
    Status getStatus();
    boolean associationStop();
}
