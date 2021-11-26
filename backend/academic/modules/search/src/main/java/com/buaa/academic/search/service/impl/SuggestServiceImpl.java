package com.buaa.academic.search.service.impl;

import com.buaa.academic.search.service.SuggestService;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SuggestServiceImpl implements SuggestService {

    private static final String prefix = "suggest-";

    @Autowired
    private ElasticsearchRestTemplate template;

    @Value("${spring.elasticsearch.highlight.pre-tag}")
    String preTag;

    @Value("${spring.elasticsearch.highlight.post-tag}")
    String postTag;

    @Override
    public <T> List<String> completionSuggest(Class<T> target, String text, String field, int size) {
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion(prefix + field,
                        SuggestBuilders.completionSuggestion("completion")
                                .prefix(text)
                                .skipDuplicates(true)
                                .size(size));
        Suggestion<Entry<Option>> suggestion = template
                .suggest(suggestBuilder, target)
                .getSuggest()
                .getSuggestion(prefix + "completion");
        List<String> suggestionWords = new ArrayList<>();
        for (Entry<Option> entry : suggestion) {
            for (Option option : entry) {
                // TODO 2021/11/27: add highlight to this feature
                suggestionWords.add(option.getText().toString());
            }
        }
        return suggestionWords;
    }

    @Override
    public <T> List<String> correctionSuggest(Class<T> target, String text, String[] phrases, int size) {
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        for (String field : phrases) {
            String label = prefix + field;
            suggestBuilder.addSuggestion(label,
                    SuggestBuilders.phraseSuggestion(field)
                            .addCandidateGenerator(new DirectCandidateGeneratorBuilder(field)
                                    .suggestMode("popular"))
                            .text(text)
                            .maxErrors(2.0f)
                            .highlight(preTag, postTag)
                            .size(size));
        }
        Suggest suggest = template
                .suggest(suggestBuilder, target)
                .getSuggest();
        List<Option> options = new ArrayList<>();
        for (String field : phrases) {
            Suggestion<Entry<Option>> suggestion = suggest.getSuggestion(prefix + field);
            for (Entry<Option> entry : suggestion) {
                for (Option option : entry) {
                    options.add(option);
                }
            }
        }
        options.sort((o1, o2) -> {
            float diff = o2.getScore() - o1.getScore();
            if (diff > 0)
                return 1;
            else if (diff < 0)
                return -1;
            return 0;
        });
        List<String> suggestionWords = new ArrayList<>();
        for (int i = 0; i < size && i < options.size(); ++i) {
            suggestionWords.add(options.get(i).getHighlighted().toString());
        }
        return suggestionWords;
    }

}
