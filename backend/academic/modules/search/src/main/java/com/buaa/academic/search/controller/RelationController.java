package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.item.PaperItem;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.model.response.ScrollPage;
import com.buaa.academic.search.model.vo.Relations;
import com.buaa.academic.search.service.InfoService;
import com.buaa.academic.search.service.RelationService;
import io.swagger.annotations.Api;
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
    public Result<ScrollPage<PaperItem>> references(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                    @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<PaperItem>> result = new Result<>();
        Paper paper = infoService.findDocument(Paper.class, id);
        if (paper == null)
            return result.withFailure(ExceptionType.NOT_FOUND);
        Relations<PaperItem> relations = relationService.searchReferences(Paper.class, paper.getReferences(), page);
        ScrollPage<PaperItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getRelations());
        return result.withData(scroll);
    }

    @GetMapping("/citations/{id}/{page}")
    public Result<ScrollPage<PaperItem>> citations(@PathVariable(name = "id") @NotBlank @Length(min = 20, max = 20) String id,
                                                   @PathVariable(name = "page") @PositiveOrZero int page) {
        Result<ScrollPage<PaperItem>> result = new Result<>();
        if (!infoService.hasDocument(Paper.class, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        Relations<PaperItem> relations = relationService.searchRelations(Paper.class, id, "references", page);
        ScrollPage<PaperItem> scroll = new ScrollPage<>(relations.hasMore(), relations.getRelations());
        return result.withData(scroll);
    }

}
