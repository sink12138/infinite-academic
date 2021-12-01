package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.model.Cooperation;
import com.buaa.academic.analysis.model.SearchAggregation;
import com.buaa.academic.analysis.model.WordFrequency;
import com.buaa.academic.analysis.repository.*;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.statistic.Association;
import com.buaa.academic.document.statistic.DataPerYear;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
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
    public Result<Object> searchAggregation() {
        HttpSession session = request.getSession();
        SearchAggregation searchAggregation = analysisShowService.searchAggregating(session);
        if (searchAggregation == null)
            return new Result<>().withFailure("该搜索不支持结果过滤");
        return new Result<>().withData(searchAggregation);
    }

    @ApiOperation(value = "话题主页", notes = "包括关联分析、发表统计分析")
    @GetMapping("/topic")
    public Result<Object> showTopic(@RequestParam(value = "name") @NotNull String name) {
        Topic topic = TopicDao.getTopicByName(name, template);
        if (topic == null)
            return new Result<>().withFailure("话题不存在");
        return new Result<>().withData(topic);
    }

    @ApiOperation(value = "学科主页", notes = "包括关联分析、发表统计分析")
    @GetMapping("/subject")
    public Result<Object> showSubject(@RequestParam(value = "name") @NotNull String name) {
        Subject subject = SubjectDao.getSubjectByName(name, template);
        if (subject == null)
            return new Result<>().withFailure("学科不存在");
        return new Result<>().withData(subject);
    }

    @ApiOperation(value = "热门学科", notes = "获取当前热点最高的前十个学科")
    @GetMapping("/hotTopics")
    public Result<Object> hotTopics() {
       return new Result<>().withData(analysisShowService.getHotTopics());
    }

    @ApiOperation(value = "热门话题", notes = "获取当前热点最高的前十个话题")
    @GetMapping("/hotSubjects")
    public Result<Object> hotSubjects() {
        return new Result<>().withData(analysisShowService.getHotSubjects());
    }

    @ApiOperation(value = "话题关联分析", notes = "用于搜索时相关推荐")
    @GetMapping("/topicAssociation")
    public Result<Object> topicAssociationAnalysis(@RequestParam(value = "name") @NotNull String name) {
        Topic topic = TopicDao.getTopicByName(name, template);
        if (topic == null)
            return new Result<>().withFailure("话题不存在");
        ArrayList<Association> associations = topic.getAssociationTopics();
        return new Result<>().withData(associations);
    }

    @ApiOperation(value = "学科关联分析", notes = "用于搜索时相关推荐")
    @GetMapping("/subjectAssociation")
    public Result<Object> subjectAssociationAnalysis(@RequestParam(value = "name") @NotNull String name) {
        Subject subject = SubjectDao.getSubjectByName(name, template);
        if (subject == null)
            return new Result<>().withFailure("学科不存在");
        ArrayList<Association> associations = subject.getAssociationSubjects();
        return new Result<>().withData(associations);
    }

    @ApiOperation(value = "每年发文统计", notes = "用于机构、学者、期刊的发文统计图，" +
            "type类型可为researcher、institution和journal，id即为分析目标的id")
    @GetMapping("/pubStaticByYear")
    public Result<Object> publicationStaticByYear(@RequestParam(value = "type") @NotNull String type,
                                                  @RequestParam(value = "id") @NotNull String id) {
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (checkRes == null)
            return new Result<>().withFailure("该类型不支持发文统计");
        if (!checkRes)
            return new Result<>().withFailure("id不存在");
        ArrayList<DataPerYear> data = analysisShowService.getPublicationStatic(type, id);
        return new Result<>().withData(data);
    }

    @ApiOperation(value = "话题统计", notes = "学者、机构、期刊发文参与的话题统计")
    @GetMapping("/topicStatic")
    public Result<Object> topicStatic(@RequestParam(value = "type") @NotNull String type,
                                      @RequestParam(value = "id") @NotNull String id) {
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (checkRes == null)
            return new Result<>().withFailure("该类型不支持话题统计");
        if (!checkRes)
            return new Result<>().withFailure("id不存在");
        ArrayList<WordFrequency> wordFrequencies = analysisShowService.topicFrequencyStatic(type, id);
        return new Result<>().withData(wordFrequencies);
    }


    @GetMapping("/cooperation")
    public Result<Object> getCooperation(@RequestParam(value = "type") @NotNull String type,
                                         @RequestParam(value = "id") @NotNull String id) {
        if (type.equals("journal"))
            return new Result<>().withFailure("该类型不支合作关系统计");
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (checkRes == null)
            return new Result<>().withFailure("该类型不支合作关系统计");
        if (!checkRes)
            return new Result<>().withFailure("id不存在");
        ArrayList<Cooperation> cooperation = analysisShowService.getCooperativeRelations(type, id);
        return new Result<>().withData(cooperation);
    }


}
