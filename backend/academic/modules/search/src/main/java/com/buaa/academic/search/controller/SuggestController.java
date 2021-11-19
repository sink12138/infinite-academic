package com.buaa.academic.search.controller;

import com.buaa.academic.model.web.Result;
import com.buaa.academic.tool.util.StringUtils;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/suggest")
@Validated
@Api(tags = "检索词实时建议")
public class SuggestController {

    @GetMapping("/{entity}")
    @ApiOperation(
            value = "建议检索词",
            notes = "<b>----------本接口已弃用，请不要请求本接口----------</b></br>" +
                    "用于在用户输入搜索关键词时提供检索建议。路径变量entity指定了建议的来源类型，允许的各值及含义如下：</br>" +
                    "<b>paper</b> - 用于在用户检索论文（和智能检索）时提供建议，建议词来源为库中论文的标题、关键词、学科分类、话题分类</br>" +
                    "<b>journal</b> - 用于在用户检索期刊时提供建议，建议词来源为库中期刊的标题</br>" +
                    "<b>institution</b> - 用于在用户检索科研机构时提供建议，建议词来源为库中科研机构的名称</br>" +
                    "<b>patent</b> - 用于在用户检索专利时提供建议，建议词来源为库中专利的标题")
    @ApiImplicitParam(name = "text", value = "用户已输入的检索词，不可为null或空")
    public Result<List<String>> suggest(@PathVariable(name = "entity") @AllowValues({"paper", "journal", "institution", "patent"}) String entity,
                                        @RequestParam("text") @NotNull @NotBlank String text) {
        return new Result<List<String>>().withFailure("Deprecated API. entity=" + entity + ",text=" + StringUtils.strip(text, 64));
    }

}
