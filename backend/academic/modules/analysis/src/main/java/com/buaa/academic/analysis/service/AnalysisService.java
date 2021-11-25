package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.Status;

public interface AnalysisService {
    boolean start();
    Status getStatus();
    void stop();
}
