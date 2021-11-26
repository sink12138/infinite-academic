package com.buaa.academic.analysis.service;

import com.buaa.academic.analysis.model.Status;

public interface AnalysisUpdateService {
    boolean start();
    Status getStatus();
    void stop();
}
