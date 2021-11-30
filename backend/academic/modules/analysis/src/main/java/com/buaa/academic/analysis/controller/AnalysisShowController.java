package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.document.statistic.Association;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@RequestMapping("/show")
@RestController()
public class AnalysisShowController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value = "搜索结果聚类分析")
    @GetMapping("/searchAgg")
    public Result<Object> searchAggregation() {
        HttpSession session = request.getSession();

        return new Result<>();
    }

    @ApiOperation(value = "话题主页，包括关联分析、发表统计分析以及机构、杂志、学者排名分析")
    @GetMapping("/topic")
    public Result<Object> showTopic(@RequestParam(value = "name") String name) {
        return new Result<>().withData(subjectRepository.findSubjectById(name));
    }

    @ApiOperation(value = "学科主页，包括关联分析、发表统计分析以及机构、杂志、学者排名分析")
    @GetMapping("/subject")
    public Result<Object> showSubject(@RequestParam(value = "name") String name) {
        return new Result<>().withData(topicRepository.findTopicById(name));
    }

    @ApiOperation(value = "话题关联分析，用于搜索时相关推荐")
    @GetMapping("/topicAssociation")
    public Result<Object> topicAssociationAnalysis(@RequestParam(value = "name") String name) {
        ArrayList<Association> associations = topicRepository.findTopicByName(name).getAssociationTopics();
        return new Result<>().withData(associations);
    }

    @ApiOperation(value = "学科关联分析，用于搜索时相关推荐")
    @GetMapping("/subjectAssociation")
    public Result<Object> subjectAssociationAnalysis(@RequestParam(value = "name") String name) {
        ArrayList<Association> associations = subjectRepository.findSubjectByName(name).getAssociationSubjects();
        return new Result<>().withData(associations);
    }

    @ApiOperation(value = "学者分析，包括关联分析、引用、发表、发明、话题、学科统计分析")
    @GetMapping("/researcherAnalysis")
    public Result<Object> researcherAnalysis() {
        return new Result<>();
    }

    @ApiOperation(value = "文献分析，包括关联分析、引用统计分析")
    @GetMapping("/paperAssociation")
    public Result<Object> paperAssociationAnalysis() {
        return new Result<>();
    }

    @ApiOperation(value = "机构分析，包括关联分析、引用、发表、发明、话题、学科统计分析")
    @GetMapping("/institutionAnalysis")
    public Result<Object> institutionAnalysis() {
        return new Result<>();
    }

    @ApiOperation(value = "期刊分析，包括引用、发表、话题、学科统计分析")
    @GetMapping("/journalAnalysis")
    public Result<Object> journalAnalysis() {
        return new Result<>();
    }
}
