package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.model.response.SuggestBox;
import com.buaa.academic.search.service.SuggestService;
import com.buaa.academic.tool.util.StringUtils;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/suggest")
@Validated
@Api(tags = "检索实时建议")
public class SuggestController {

    @Autowired
    private SuggestService suggestService;

    @PostMapping("/{entity}")
    @ApiOperation(
            value = "建议检索词",
            notes = "用于在用户输入搜索关键词时提供检索建议。路径变量entity指定了建议的来源类型，允许的各值及含义如下：</br>" +
                    "<b>paper</b> - 检索论文（和智能检索）时提供建议，建议词来源为库中论文的标题、关键词、学科分类</br>" +
                    "<b>journal</b> - 检索期刊时提供建议，建议词来源为库中期刊的标题</br>" +
                    "<b>institution</b> - 检索科研机构时提供建议，建议词来源为库中科研机构的名称</br>" +
                    "<b>patent</b> - 检索专利时提供建议，建议词来源为库中专利的标题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "指定需要建议的实体类别", example = "paper"),
            @ApiImplicitParam(name = "text", value = "用户已输入的检索词，不可为null或空", example = "数据哇掘")})
    public Result<SuggestBox> suggest(@PathVariable(name = "entity") @AllowValues({"paper", "journal", "institution", "patent"}) String entity,
                                      @RequestParam("text") @NotNull @NotBlank String text) {
        Result<SuggestBox> result = new Result<>();
        SuggestBox suggestBox = new SuggestBox();
        text = StringUtils.strip(text, 64);
        Class<?> target;
        String[] phrases;
        switch (entity) {
            case "paper" -> {
                target = Paper.class;
                phrases = new String[] { "title.phrase", "keywords.phrase", "subjects.phrase",
                        "title.raw", "keywords.raw", "subjects.raw" };
            }
            case "journal" -> {
                target = Journal.class;
                phrases = new String[] { "title.phrase", "title.raw" };
            }
            case "institution" -> {
                target = Institution.class;
                phrases = new String[] { "name.phrase", "name.raw" };
            }
            case "patent" -> {
                target = Patent.class;
                phrases = new String[] { "title.phrase", "title.raw" };
            }
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        List<String> completion = suggestService.completionSuggest(target, text, "completion", 10);
        suggestBox.setCompletion(completion);
        if (completion.size() <= 2)
            suggestBox.setCorrection(suggestService.correctionSuggest(target, text, phrases, 3));
        return result.withData(suggestBox);
    }

}
