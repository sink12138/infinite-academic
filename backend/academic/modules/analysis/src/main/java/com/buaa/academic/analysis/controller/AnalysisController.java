package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.service.AnalysisService;
import com.buaa.academic.model.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @PostMapping("/start")
    public Result<Object> startAnalysis() {
        if(analysisService.associationAnalysis())
            return new Result<>();
        else
            return new Result<>().withFailure("Analysis program has been running...");
    }

    @GetMapping("/status")
    public Result<Object> checkAnalysisStatus() {
        return new Result<>().withData(analysisService.getStatus());
    }

    @PostMapping("/stop")
    public Result<Void> stopAnalysis() {
        analysisService.associationStop();
        return new Result<>();
    }

}
