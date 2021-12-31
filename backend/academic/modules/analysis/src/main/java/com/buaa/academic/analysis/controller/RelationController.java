package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.analysis.model.Bucket;
import com.buaa.academic.analysis.model.EntityBucket;
import com.buaa.academic.analysis.model.TopRanks;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Researcher;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@Api(tags = "关系网络与关联统计")
public class RelationController {

    @Autowired
    private AnalysisShowService analysisShowService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @GetMapping("/cooperation/{entity}/{id}")
    @ApiOperation(
            value = "合作关系统计",
            notes = "学者和机构的合作关系网络统计，代表了 学者<->学者 和 机构<->机构 关联。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别（researcher/institution）"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc"),
            @ApiImplicitParam(name = "num", value = "合作关系条目数量，最大为30。")})
    public Result<List<EntityBucket>> cooperation(@PathVariable("entity") @AllowValues({"researcher", "institution"}) String entity,
                                                  @PathVariable("id") @NotBlank @Length(min = 20, max = 20) String id,
                                                  @RequestParam("num") @Positive @Max(30) int num) {
        Result<List<EntityBucket>> result = new Result<>();
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
        List<EntityBucket> cooperation = analysisShowService.getCooperativeRelations(entity, id, num);
        return result.withData(cooperation);
    }

    @GetMapping("/participants/{entity}")
    @ApiOperation(
            value = "话题学科活跃参与者",
            notes = "在某一学科或话题下参与（发文）量前十的机构、期刊或者学者，代表了 学科/话题->实体 关联。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "参与者实体的类别（researcher/journal/institution）"),
            @ApiImplicitParam(name = "type", value = "区分话题/学科（topic/subject）"),
            @ApiImplicitParam(name = "name", value = "学科/话题名称", example = "机器学习"),
            @ApiImplicitParam(name = "num", value = "\"热门Top N\"中的\"N\"，最大为20。")})
    public Result<List<EntityBucket>> topParticipants(@PathVariable("entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                                      @RequestParam("type") @AllowValues({"subject", "topic"})  String type,
                                                      @RequestParam("name") @NotBlank String name,
                                                      @RequestParam("num") @Positive @Max(20) int num) {
        Result<List<EntityBucket>> result = new Result<>();
        if (type.equals("topic")) {
            Topic topic = topicRepository.findTopicByName(name);
            if (topic == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
        } else {
            Subject subject = subjectRepository.findSubjectByName(name);
            if (subject == null)
                return result.withFailure(ExceptionType.NOT_FOUND);
        }
        return result.withData(analysisShowService.topEntities(type, name, entity, num));
    }

    @GetMapping("/participation/{entity}/{id}")
    @ApiOperation(
            value = "话题学科参与统计",
            notes = "学者、机构、期刊发文参与的话题或者学科统计，代表了 实体->学科/话题 关联。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别", allowableValues = "researcher, journal, institution"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc"),
            @ApiImplicitParam(name = "num", value = "词云项目数量，最大为20。")})
    public Result<TopRanks<Bucket>> frequencies(@PathVariable("entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                        @PathVariable("id") @NotBlank @Length(min = 20, max = 20) String id,
                                        @RequestParam("num") @Positive @Max(20) int num) {
        Result<TopRanks<Bucket>> result = new Result<>();
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
        List<Bucket> subjectFrequencies = analysisShowService.wordFrequencyStatistics(entity, id, "subjects", num);
        List<Bucket> topicFrequencies = analysisShowService.wordFrequencyStatistics(entity, id, "topics", num);
        TopRanks<Bucket> ranks = new TopRanks<>(subjectFrequencies, topicFrequencies);
        return result.withData(ranks);
    }

}
