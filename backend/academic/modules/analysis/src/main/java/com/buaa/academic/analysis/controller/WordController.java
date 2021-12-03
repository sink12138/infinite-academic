package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Api(tags = "学科与话题基本信息")
public class WordController {

    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private TopicRepository topicRepository;

    @GetMapping("/subject")
    @ApiOperation(value = "学科基本信息", notes = "包括关联分析、发表统计分析")
    @ApiImplicitParam(name = "name", value = "要访问的学科名称", example = "数学")
    public Result<Subject> subject(@RequestParam(value = "name") String name) {
        Result<Subject> result = new Result<>();
        Subject subject = subjectRepository.findSubjectByName(name);
        if (subject == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(subject);
    }

    @GetMapping("/topic")
    @ApiOperation(value = "话题基本信息", notes = "包括关联分析、发表统计分析")
    @ApiImplicitParam(name = "name", value = "要访问的话题名称", example = "人工智能")
    public Result<Topic> topic(@RequestParam(value = "name") String name) {
        Result<Topic> result = new Result<>();
        Topic topic = topicRepository.findTopicByName(name);
        if (topic == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        return result.withData(topic);
    }

}
