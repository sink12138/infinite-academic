package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.analysis.repository.*;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.statistic.Association;
import com.buaa.academic.document.statistic.DataByYear;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
            return result.withFailure("该搜索不支持结果过滤或尚未进行搜索");
        return result.withData(searchAggregation);
    }

    @ApiOperation(value = "话题主页", notes = "包括关联分析、发表统计分析")
    @ApiImplicitParam(name = "name", value = "要访问的话题名称", example = "人工智能")
    @GetMapping("/topic")
    public Result<Topic> showTopic(@RequestParam(value = "name") @NotNull String name) {
        Result<Topic> result = new Result<>();
        Topic topic = TopicDao.getTopicByName(name, template);
        if (topic == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(topic);
    }

    @ApiOperation(value = "学科主页", notes = "包括关联分析、发表统计分析")
    @ApiImplicitParam(name = "name", value = "要访问的学科名称", example = "数学")
    @GetMapping("/subject")
    public Result<Subject> showSubject(@RequestParam(value = "name") @NotNull String name) {
        Result<Subject> result = new Result<>();
        Subject subject = SubjectDao.getSubjectByName(name, template);
        if (subject == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(subject);
    }

    @ApiOperation(value = "热门词汇", notes = "获取当前热点最高的前十个学科或话题")
    @ApiImplicitParam(name = "field", value = "要统计的方面", allowableValues = "topics, subjects")
    @GetMapping("/hot/{field}")
    public Result<ArrayList<HotWord>> hotTopics(@PathVariable("field") @AllowValues({"topics", "subjects"}) String field) {
       return new Result<ArrayList<HotWord>>().withData(analysisShowService.getHotWords(field));
    }

    @ApiOperation(value = "学科话题关联分析", notes = "用于搜索时相关推荐")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "field", value = "要统计的方面", allowableValues = "topics, subjects"),
        @ApiImplicitParam(name = "name", value = "要获取关联信息的话题或学科的名称", example = "数据挖掘")
    })
    @GetMapping("/{field}/association")
    public Result<List<Association>> topicAssociationAnalysis(@PathVariable("field") @AllowValues({"topics", "subjects"}) String field,
                                                              @RequestParam(value = "name") @NotNull String name) {
        Result<List<Association>> result = new Result<>();
        List<Association> associations;
        if (field.equals("topic")) {
            Topic topic = TopicDao.getTopicByName(name, template);
            if (topic == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            associations = topic.getAssociationTopics();
        } else {
            Subject subject = SubjectDao.getSubjectByName(name, template);
            if (subject == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
            associations = subject.getAssociationSubjects();
        }
        return result.withData(associations);
    }

    @ApiOperation(value = "每年发文统计", notes = "用于机构、学者、期刊的发文统计图")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "type", value = "要进行统计的实体的类别", allowableValues = "researcher, journal, institution"),
        @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc")
    })
    @GetMapping("/{type}/pubStaticByYear")
    public Result<DataByYear> publicationStaticByYear(@PathVariable("type") @NotNull
                                                                      @AllowValues({"researcher", "journal", "institution"}) String type,
                                                                 @RequestParam(value = "id") @NotNull String id) {
        Result<DataByYear> result = new Result<>();
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (!checkRes)
            return result.withFailure(ExceptionType.NOT_FOUND);
        DataByYear data = analysisShowService.getPublicationStatic(type, id);
        return result.withData(data);
    }

    @ApiOperation(value = "词频统计", notes = "学者、机构、期刊发文参与的话题或者学科统计，用于词云展示")
    @GetMapping("/{type}/static/{field}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "要进行统计的实体的类别", allowableValues = "researcher, journal, institution"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc"),
            @ApiImplicitParam(name = "field", value = "要进行统计的方面", allowableValues = "topics, subjects")
    })
    public Result<ArrayList<Frequency>> topicStatic(@PathVariable("type") @NotNull
                                                                    @AllowValues({"researcher", "journal", "institution"}) String type,
                                                    @RequestParam(value = "id") @NotNull String id,
                                                    @PathVariable("field") @NotNull @AllowValues({"topics", "subjects"}) String field) {
        Result<ArrayList<Frequency>> result = new Result<>();
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (!checkRes)
            return result.withFailure(ExceptionType.NOT_FOUND);
        ArrayList<Frequency> wordFrequencies = analysisShowService.wordFrequencyStatic(type, id, field);
        return result.withData(wordFrequencies);
    }

    @ApiOperation(value = "合作关系统计", notes = "学者和机构的合作关系网络统计")
    @GetMapping("/{type}/cooperation")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "要进行统计的实体的类别", allowableValues = "researcher, institution"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc")
    })
    public Result<ArrayList<EntityFrequency>> getCooperation(@PathVariable("type") @NotNull
                                                                     @AllowValues({"researcher", "institution"}) String type,
                                         @RequestParam(value = "id") @NotNull String id) {
        Result<ArrayList<EntityFrequency>> result = new Result<>();
        Boolean checkRes = analysisShowService.checkTargetExist(type, id);
        if (!checkRes)
            return result.withFailure(ExceptionType.NOT_FOUND);
        ArrayList<EntityFrequency> cooperation = analysisShowService.getCooperativeRelations(type, id);
        return result.withData(cooperation);
    }

    @ApiOperation(value = "获取顶级机构、期刊、学者", notes = "在某一学科或话题下发文量前十的机构、期刊或者学者")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "field", value = "要进行统计的方面", allowableValues = "topic, subject"),
            @ApiImplicitParam(name = "name", value = "学科或者话题名称", example = "机器学习"),
            @ApiImplicitParam(name = "type", value = "要进行统计的实体的类别", allowableValues = "researchers, journals, institutions"),
    })
    @GetMapping("/{field}/top/{type}")
    public Result<ArrayList<EntityFrequency>> getToptopEntities(@PathVariable("field") @AllowValues({"subject", "topic"})  String field,
                                                           @RequestParam(value = "name") @NotNull String name,
                                                           @PathVariable("type") @AllowValues({"researchers", "journals", "institutions"}) String type) {
        Result<ArrayList<EntityFrequency>> result = new Result<>();
        if (field.equals("topic")) {
            Topic topic = TopicDao.getTopicByName(name, template);
            if (topic == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
        } else {
            Subject subject = SubjectDao.getSubjectByName(name, template);
            if (subject == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        return result.withData(analysisShowService.topEntities(field, name, type));
    }
}
