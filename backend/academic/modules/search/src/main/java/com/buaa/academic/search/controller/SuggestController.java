package com.buaa.academic.search.controller;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Patent;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.search.service.SuggestService;
import com.buaa.academic.tool.util.StringUtils;
import com.buaa.academic.tool.validator.AllowValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("/suggest")
public class SuggestController {

    @Autowired
    private SuggestService suggestService;

    @GetMapping("/{entity}")
    public Result<List<String>> suggest(@PathVariable(name = "entity") @AllowValues({"paper", "journal", "institution", "patent"}) String entity,
                                        @RequestParam("text") @NotNull @NotBlank String text) {
        Result<List<String>> result = new Result<>();
        text = StringUtils.strip(text, 64);
        Class<?> target;
        String field;
        switch (entity) {
            case "paper" -> {
                target = Paper.class;
                field = "completion";
            }
            case "journal" -> {
                target = Journal.class;
                field = "title";
            }
            case "institution" -> {
                target = Institution.class;
                field = "name";
            }
            case "patent" -> {
                target = Patent.class;
                field = "title";
            }
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        List<String> suggestions = suggestService.completionSuggest(target, text);
        if (suggestions.size() <= 2) {
            suggestions = suggestService.phraseSuggest(target, text, field);
        }
        return result.withData(suggestions);
    }

}
