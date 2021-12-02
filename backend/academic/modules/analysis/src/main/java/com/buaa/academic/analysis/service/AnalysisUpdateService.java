package com.buaa.academic.analysis.service;

import com.buaa.academic.model.web.Schedule;

public interface AnalysisUpdateService {
    boolean start();
    Schedule getStatus();
    void stop();
}
