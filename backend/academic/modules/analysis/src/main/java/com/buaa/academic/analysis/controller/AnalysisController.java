package com.buaa.academic.analysis.controller;

//import com.buaa.academic.analysis.service.AnalysisService;
import com.buaa.academic.model.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {
/*
    @Autowired
    private AnalysisService analysisService;
*/

    @PostMapping("/testAnalysis")
    public Result<Void> testAnalysis() {
        // analysisService.associationAnalysis();
        return new Result<>();
    }
}
