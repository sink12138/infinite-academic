package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.document.entity.item.PatentItem;
import com.buaa.academic.document.entity.item.ResearcherItem;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.model.response.ScrollPage;
import com.buaa.academic.search.model.vo.Relations;
import com.buaa.academic.search.service.InfoService;
import com.buaa.academic.search.service.RelationService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/relation")
@Validated
@Api(tags = "实体关联信息")
public class RelationController {

    @Autowired
    private InfoService infoService;

    @Autowired
    private RelationService relationService;

    @GetMapping("/references/{id}/{page}")
    @ApiOperation(value = "参考文献", notes = "通过路径变量id来获取一篇论文的参考文献列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "论文ID"),
            @ApiImplicitParam(name = "page", value = "页码编号，从0开始")})
    public Result<ScrollPage<PaperItem>> references(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                    @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<PaperItem>> result = new Result<>();
        Paper paper = infoService.findDocument(Paper.class, id);
        if (paper == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        Relations<PaperItem> relations = relationService.searchReferences(paper.getReferences(), page);
        ScrollPage<PaperItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getItems());
        return result.withData(scroll);
    }

    @GetMapping("/citations/{id}/{page}")
    @ApiOperation(value = "引证文献", notes = "通过路径变量id来获取一篇论文的引证文献（即引用这篇论文的论文）列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "论文ID"),
            @ApiImplicitParam(name = "page", value = "页码编号，从0开始")})
    public Result<ScrollPage<PaperItem>> citations(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                   @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<PaperItem>> result = new Result<>();
        if (!infoService.existsDocument(Paper.class, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        Relations<PaperItem> relations = relationService.searchRelations(Paper.class, id, "references", page);
        ScrollPage<PaperItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getItems());
        return result.withData(scroll);
    }

    @ApiOperation(
            value = "出版文献",
            notes = "通过路径变量id来获取某个实体出版或发表的文献列表。路径变量entity可以为以下值：</br>" +
                    "<b>researcher</b> - 代表查询某位科研人员发表过的论文</br>" +
                    "<b>journal</b> - 代表查询某份期刊登载过的论文</br>" +
                    "<b>institution</b> - 代表查询某个科研机构发表过的论文")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "实体类别"),
            @ApiImplicitParam(name = "id", value = "实体ID"),
            @ApiImplicitParam(name = "page", value = "页码编号，从0开始")})
    @GetMapping("/publications/{entity}/{id}/{page}")
    public Result<ScrollPage<PaperItem>> publications(@PathVariable(name = "entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                                      @PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                      @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<PaperItem>> result = new Result<>();
        String field;
        switch (entity) {
            case "researcher" -> {
                if (!infoService.existsDocument(Researcher.class, id))
                    return result.withFailure(ExceptionType.NOT_FOUND);
                field = "authors.id";
            }
            case "journal" -> {
                if (!infoService.existsDocument(Journal.class, id))
                    return result.withFailure(ExceptionType.NOT_FOUND);
                field = "journal.id";
            }
            case "institution" -> {
                if (!infoService.existsDocument(Institution.class, id))
                    return result.withFailure(ExceptionType.NOT_FOUND);
                field = "institutions.id";
            }
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        Relations<PaperItem> relations = relationService.searchRelations(Paper.class, id, field, page);
        ScrollPage<PaperItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getItems());
        return result.withData(scroll);
    }

    @GetMapping("/scholars/{id}/{page}")
    @ApiOperation(value = "机构学者", notes = "通过路径变量id来获取归属于某一个科研机构的学者列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "机构ID"),
            @ApiImplicitParam(name = "page", value = "页码编号，从0开始")})
    public Result<ScrollPage<ResearcherItem>> scholars(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                       @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<ResearcherItem>> result = new Result<>();
        if (!infoService.existsDocument(Researcher.class, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        Relations<ResearcherItem> relations = relationService.searchRelations(Researcher.class, id, "currentInst.id", page);
        ScrollPage<ResearcherItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getItems());
        return result.withData(scroll);
    }

    @GetMapping("/inventions/{id}/{page}")
    @ApiOperation(value = "发明专利", notes = "通过路径变量id来获取一位科研人员发明的专利")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "科研人员ID"),
            @ApiImplicitParam(name = "page", value = "页码编号，从0开始")})
    public Result<ScrollPage<PatentItem>> inventions(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                     @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<PatentItem>> result = new Result<>();
        if (!infoService.existsDocument(Patent.class, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        Relations<PatentItem> relations = relationService.searchRelations(Patent.class, id, "inventors.id", page);
        ScrollPage<PatentItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getItems());
        return result.withData(scroll);
    }

}
