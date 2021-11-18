package com.buaa.academic.analysis.service.impl;

import com.buaa.academic.analysis.service.AnalysisService;
import com.buaa.academic.analysis.service.impl.fpg.FPGMainClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public void associationAnalysis() {
        FPGMainClass fpgMainClass = new FPGMainClass(0.4, 0.6, false, "topic");
        fpgMainClass.setTemplate(template);
        new Thread(fpgMainClass).start();
    }
}
