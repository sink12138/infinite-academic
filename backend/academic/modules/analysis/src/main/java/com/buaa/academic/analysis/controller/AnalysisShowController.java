package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.analysis.repository.*;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.statistic.Association;
import com.buaa.academic.document.statistic.DataPerYear;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/show")
@RestController()
@Validated
public class AnalysisShowController {
    @Autowired
    private AnalysisShowService analysisShowService;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value = "搜索结果聚类分析")
    @GetMapping("/searchAgg")
    public Result<SearchAggregation> searchAggregation() {
        Result<SearchAggregation> result = new Result<>();
        HttpSession session = request.getSession();
        SearchAggregation searchAggregation = analysisShowService.searchAggregating(session);
        if (searchAggregation == null)
            return result.withFailure("该搜索不支持结果过滤");
        return result.withData(searchAggregation);
    }

    @ApiOperation(value = "话题主页", notes = "包括关联分析、发表统计分析")
    @GetMapping("/topic")
    public Result<Topic> showTopic(@RequestParam(value = "name") @NotNull String name) {
        Result<Topic> result = new Result<>();
        Topic topic = TopicDao.getTopicByName(name, template);
        if (topic == null)
            return result.withFailure("话题不存在");
        return result.withData(topic);
    }

    @ApiOperation(value = "学科主页", notes = "包括关联分析、发表统计分析")
    @GetMapping("/subject")
    public Result<Subject> showSubject(@RequestParam(value = "name") @NotNull String name) {
        Result<Subject> result = new Result<>();
        Subject subject = SubjectDao.getSubjectByName(name, template);
        if (subject == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(subject);
    }

    @ApiOperation(value = "热门学科", notes = "获取当前热点最高的前十个学科")
    @GetMapping("/hotTopics")
    public Result<ArrayList<SimpleTopic>> hotTopics() {
       return new Result<ArrayList<SimpleTopic>>().withData(analysisShowService.getHotTopics());
    }

    @ApiOperation(value = "热门话题", notes = "获取当前热点最高的前十个话题")
    @GetMapping("/hotSubjects")
    public Result<ArrayList<SimpleSubject>> hotSubjects() {
        return new Result<ArrayList<SimpleSubject>>().withData(analysisShowService.getHotSubjects());
    }

    @ApiOperation(value = "话题关联分析", notes = "用于搜索时相关推荐")
    @GetMapping("/topicAssociation")
    public Result<List<Association>> topicAssociationAnalysis(@RequestParam(value = "name") @NotNull String name) {
        Result<List<Association>> result = new Result<>();
        Topic topic = TopicDao.getTopicByName(name, template);
        if (topic == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        List<Association> associations = topic.getAssociationTopics();
        return result.withData(associations);
    }

    @ApiOperation(value = "学科关联分析", notes = "用于搜索时相关推荐")
    @GetMapping("/subjectAssociation")
    public Result<List<Association>> subjectAssociationAnalysis(@RequestParam(value = "name") @NotNull String name) {
        Result<List<Association>> result = new Result<>();
        Subject subject = SubjectDao.getSubjectByName(name, template);
        if (subject == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        List<Association> associations = subject.getAssociationSubjects();
        return result.withData(associations);
    }

    @ApiOperation(value = "每年发文统计", notes = "用于机构、学者、期刊的发文统计图，" +
            "type类型可为researcher、institution和journal，id即为分析目标的id")
    @GetMapping("/pubStaticByYear")
    public Result<ArrayList<DataPerYear>> publicationStaticByYear(@RequestParam(value = "type") @NotNull String type,
                                                  @RequestParam(value = "id") @NotNull String id) {
        Result<ArrayList<DataPerYear>> result = new Result<>();
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (checkRes == null)
            return result.withFailure("该类型不支持发文统计");
        if (!checkRes)
            return result.withFailure(ExceptionType.NOT_FOUND);
        ArrayList<DataPerYear> data = analysisShowService.getPublicationStatic(type, id);
        return result.withData(data);
    }

    @ApiOperation(value = "话题统计", notes = "学者、机构、期刊发文参与的话题统计")
    @GetMapping("/topicStatic")
    public Result<ArrayList<WordFrequency>> topicStatic(@RequestParam(value = "type") @NotNull String type,
                                      @RequestParam(value = "id") @NotNull String id) {
        Result<ArrayList<WordFrequency>> result = new Result<>();
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (checkRes == null)
            return result.withFailure("该类型不支持话题统计");
        if (!checkRes)
            return result.withFailure(ExceptionType.NOT_FOUND);
        ArrayList<WordFrequency> wordFrequencies = analysisShowService.topicFrequencyStatic(type, id);
        return result.withData(wordFrequencies);
    }

    @GetMapping("/cooperation")
    public Result<ArrayList<Cooperation>> getCooperation(@RequestParam(value = "type") @NotNull String type,
                                         @RequestParam(value = "id") @NotNull String id) {
        Result<ArrayList<Cooperation>> result = new Result<>();
        if (type.equals("journal"))
            return result.withFailure("该类型不支合作关系统计");
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (checkRes == null)
            return result.withFailure("该类型不支合作关系统计");
        if (!checkRes)
            return result.withFailure(ExceptionType.NOT_FOUND);
        ArrayList<Cooperation> cooperation = analysisShowService.getCooperativeRelations(type, id);
        return result.withData(cooperation);
    }

}
