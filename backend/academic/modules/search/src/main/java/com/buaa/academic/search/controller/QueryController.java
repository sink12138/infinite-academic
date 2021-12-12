package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.dao.JournalRepository;
import com.buaa.academic.search.dao.PaperRepository;
import com.buaa.academic.search.dao.PatentRepository;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/query")
@Validated
@Api(tags = "编号精确查询")
public class QueryController {

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private PatentRepository patentRepository;

    @PostMapping("/")
    @ApiOperation(
            value = "精确查询",
            notes = "使用DOI、ISSN或专利号精确查询某一论文、期刊或专利。</br>" +
                    "由于GET方法存在url不安全问题，本接口采用<b>POST</b>方法。</br>" +
                    "返回值格式为\"{entity}/{id}\"，其中entity为查询到的实体类别，id为查询到的实体ID。</br>" +
                    "可以直接将返回值拼接在\"/search/info/\"后面进行页面跳转。</br>" +
                    "<b>注意：若未找到对应实体，本接口返回success = false。</b>")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "查询类别：doi/issn/patentNum", example = "doi"),
            @ApiImplicitParam(name = "key", value = "查询编号字符串", example = "10.15881/j.cnki.cn33-1304/g4.2017.03.002")})
    public Result<String> query(@RequestParam(name = "type") @AllowValues({"doi", "issn", "patentNum"}) String type,
                                @RequestParam(name = "key") @NotEmpty @Length(max = 128) String key) {
        Result<String> result = new Result<>();
        String entity;
        String id;
        switch (type) {
            case "doi" -> {
                Paper paper = paperRepository.findTopByDoi(key);
                if (paper != null) {
                    entity = "paper";
                    id = paper.getId();
                    break;
                }
                return result.withFailure(ExceptionType.NOT_FOUND);
            }
            case "issn" -> {
                Paper paper = paperRepository.findTopByIssn(key);
                if (paper != null) {
                    entity = "paper";
                    id = paper.getId();
                    break;
                }
                Journal journal = journalRepository.findTopByIssn(key);
                if (journal != null) {
                    entity = "journal";
                    id = journal.getId();
                    break;
                }
                return result.withFailure(ExceptionType.NOT_FOUND);
            }
            case "patentNum" -> {
                Patent patent = patentRepository.findTopByPatentNum(key);
                if (patent != null) {
                    entity = "patent";
                    id = patent.getId();
                    break;
                }
                return result.withFailure(ExceptionType.NOT_FOUND);
            }
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        return result.withData(entity + "/" + id);
    }

}
