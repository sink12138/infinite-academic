package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.model.*;
import com.buaa.academic.analysis.repository.SubjectDao;
import com.buaa.academic.analysis.repository.TopicDao;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.statistic.SumPerYear;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@Api(tags = "数据分析结果展示")
public class AnalysisShowController {

    @Autowired
    private AnalysisShowService analysisShowService;

    @Autowired
    private ElasticsearchRestTemplate template;

    @GetMapping("/aggregations")
    @ApiOperation(
            value = "搜索结果聚合",
            notes = "展示搜索结果的聚合，例如年份统计、领域统计、作者统计等。</br>" +
                    "本接口依赖于搜索模块的缓存，因此不需要传递任何额外参数，但需先请求搜索模块的对应接口后再请求本接口。")
    public Result<List<SearchAggregation>> searchAggregation(HttpServletRequest request) {
        Result<List<SearchAggregation>> result = new Result<>();
        HttpSession session = request.getSession();
        List<SearchAggregation> searchAggregations = analysisShowService.searchAggregating(session);
        if (searchAggregations == null)
            return result.withFailure("该搜索不支持结果过滤或尚未进行搜索");
        return result.withData(searchAggregations);
    }

    @GetMapping("/topic")
    @ApiOperation(value = "话题主页", notes = "包括关联分析、发表统计分析")
    @ApiImplicitParam(name = "name", value = "要访问的话题名称", example = "人工智能")
    public Result<Topic> topic(@RequestParam(value = "name") String name) {
        Result<Topic> result = new Result<>();
        Topic topic = TopicDao.getTopicByName(name, template);
        if (topic == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(topic);
    }

    @GetMapping("/subject")
    @ApiOperation(value = "学科主页", notes = "包括关联分析、发表统计分析")
    @ApiImplicitParam(name = "name", value = "要访问的学科名称", example = "数学")
    public Result<Subject> subject(@RequestParam(value = "name") String name) {
        Result<Subject> result = new Result<>();
        Subject subject = SubjectDao.getSubjectByName(name, template);
        if (subject == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(subject);
    }

    @GetMapping("/hotspots")
    @ApiOperation(value = "热点学科和话题", notes = "获取当前热度最高的若干个学科或话题")
    @ApiImplicitParam(name = "num", value = "\"热门Top N\"中的\"N\"，最大为20。")
    public Result<Hotspots> hotspots(@RequestParam("num") @Positive @Max(20) int num) {
        Result<Hotspots> result = new Result<>();
        Hotspots hotspots = new Hotspots();
        hotspots.setSubjects(analysisShowService.getHotWords("subjects", num));
        hotspots.setSubjects(analysisShowService.getHotWords("topics", num));
        return result.withData(hotspots);
    }

    @GetMapping("/participants/{entity}")
    @ApiOperation(value = "获取顶级机构、期刊、学者", notes = "在某一学科或话题下参与（发文）量前十的机构、期刊或者学者（top entities in the field）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别（researcher/journal/institution）"),
            @ApiImplicitParam(name = "field", value = "要进行统计的方面（topic/subject）"),
            @ApiImplicitParam(name = "name", value = "学科或者话题名称", example = "机器学习"),
            @ApiImplicitParam(name = "num", value = "\"热门Top N\"中的\"N\"，最大为20。")})
    public Result<List<EntityFrequency>> topParticipants(@PathVariable("entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                                         @RequestParam("type") @AllowValues({"subject", "topic"})  String type,
                                                         @RequestParam("name") @NotEmpty String name,
                                                         @RequestParam("num") @Positive @Max(20) int num) {
        Result<List<EntityFrequency>> result = new Result<>();
        if (type.equals("topic")) {
            Topic topic = TopicDao.getTopicByName(name, template);
            if (topic == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
        } else {
            Subject subject = SubjectDao.getSubjectByName(name, template);
            if (subject == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        return result.withData(analysisShowService.topEntities(type, name, entity, num));
    }

    @GetMapping("/statistics/{entity}/{id}")
    @ApiOperation(value = "每年发文统计", notes = "用于机构、学者、期刊的发文统计图")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别（researcher/journal/institution）"),
        @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc")})
    public Result<SumPerYear> statistics(@PathVariable("entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                         @PathVariable("id") @NotEmpty @Length(min = 20, max = 20) String id) {
        Result<SumPerYear> result = new Result<>();
        Class<?> target;
        switch (entity) {
            case "researcher" -> target = Researcher.class;
            case "journal" -> target = Journal.class;
            case "institution" -> target = Institution.class;
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        if (!analysisShowService.existsTarget(target, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        SumPerYear data = analysisShowService.getPublicationStatic(entity, id);
        return result.withData(data);
    }

    @GetMapping("/frequencies/{entity}/{id}")
    @ApiOperation(value = "话题学科参与统计", notes = "学者、机构、期刊发文参与的话题或者学科统计，用于词云展示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别", allowableValues = "researcher, journal, institution"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc"),
            @ApiImplicitParam(name = "num", value = "词云项目数量，最大为20。")})
    public Result<WordCloud> frequencies(@PathVariable("entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                         @PathVariable("id") @NotEmpty @Length(min = 20, max = 20) String id,
                                         @RequestParam("num") @Positive @Max(20) int num) {
        Result<WordCloud> result = new Result<>();
        Class<?> target;
        switch (entity) {
            case "researcher" -> target = Researcher.class;
            case "journal" -> target = Journal.class;
            case "institution" -> target = Institution.class;
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        if (!analysisShowService.existsTarget(target, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        List<Frequency> subjectFrequencies = analysisShowService.wordFrequencyStatistics(entity, id, "subjects", num);
        List<Frequency> topicFrequencies = analysisShowService.wordFrequencyStatistics(entity, id, "topics", num);
        WordCloud cloud = new WordCloud(subjectFrequencies, topicFrequencies);
        return result.withData(cloud);
    }

    @GetMapping("/cooperation/{entity}/{id}")
    @ApiOperation(value = "合作关系统计", notes = "学者和机构的合作关系网络统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别（researcher/institution）"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc"),
            @ApiImplicitParam(name = "num", value = "合作关系条目数量，最大为30。")})
    public Result<List<EntityFrequency>> cooperation(@PathVariable("entity") @AllowValues({"researcher", "institution"}) String entity,
                                                     @PathVariable("id") @NotEmpty @Length(min = 20, max = 20) String id,
                                                     @RequestParam("num") @Positive @Max(30) int num) {
        Result<List<EntityFrequency>> result = new Result<>();
        Class<?> target;
        switch (entity) {
            case "researcher" -> target = Researcher.class;
            case "institution" -> target = Institution.class;
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        if (!analysisShowService.existsTarget(target, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        List<EntityFrequency> cooperation = analysisShowService.getCooperativeRelations(entity, id, num);
        return result.withData(cooperation);
    }

}
