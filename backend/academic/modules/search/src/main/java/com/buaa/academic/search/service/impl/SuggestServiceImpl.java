package com.buaa.academic.search.service.impl;

import com.buaa.academic.search.service.SuggestService;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.FuzzyOptions;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SuggestServiceImpl implements SuggestService {

    @Autowired
    private ElasticsearchRestTemplate template;

    private final String suggestionLabel = "text-suggest";

    @Override
    public <T> List<String> completionSuggest(Class<T> target, String text) {
        Suggest suggest = template.suggest(new SuggestBuilder()
                .addSuggestion(suggestionLabel,
                        SuggestBuilders.completionSuggestion("suggest")
                                .prefix(text, FuzzyOptions.builder()
                                        .setUnicodeAware(true)
                                        .build())
                                .analyzer("ik_optimized")
                                .size(10)
                                .skipDuplicates(true)),
                target).getSuggest();
        return getSuggestionWords(suggest);
    }

    @Override
    public <T> List<String> phraseSuggest(Class<T> target, String text, String field) {
        Suggest suggest = template.suggest(new SuggestBuilder().setGlobalText(text)
                .addSuggestion(suggestionLabel,
                        SuggestBuilders.phraseSuggestion(field)
                                .text(text)
                                .analyzer("ik_optimized")
                                .size(10)
                                .addCandidateGenerator(new DirectCandidateGeneratorBuilder(field)
                                        .suggestMode("popular"))),
                target).getSuggest();
        return getSuggestionWords(suggest);
    }

    private List<String> getSuggestionWords(Suggest suggest) {
        Suggestion<Entry<Option>> suggestion = suggest.getSuggestion(suggestionLabel);
        List<String> suggestionWords = new ArrayList<>();
        for (Entry<Option> entry : suggestion) {
            for (Option option : entry) {
                suggestionWords.add(option.getText().toString());
            }
        }
        return suggestionWords;
    }

}
