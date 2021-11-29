package com.buaa.academic.search.service.impl;

import com.buaa.academic.search.service.SuggestService;
import com.buaa.academic.search.util.HighlightManager;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse.AnalyzeToken;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SuggestServiceImpl implements SuggestService {

    private static final String prefix = "suggest-";

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private RestHighLevelClient client;

    @Value("${spring.elasticsearch.highlight.pre-tag}")
    private String preTag;

    @Value("${spring.elasticsearch.highlight.post-tag}")
    private String postTag;

    private final HighlightManager manager = new HighlightManager(preTag, postTag);

    @Override
    public <T> List<String> completionSuggest(Class<T> target, String text, String field, int size) {
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion(prefix + field,
                        SuggestBuilders.completionSuggestion(field)
                                .prefix(text)
                                .skipDuplicates(true)
                                .size(size));
        Suggestion<Entry<Option>> suggestion = template
                .suggest(suggestBuilder, target)
                .getSuggest()
                .getSuggestion(prefix + field);
        List<String> analyzedWords = analyze(target.getName().toLowerCase(), text);
        List<String> suggestionWords = new ArrayList<>();
        for (Entry<Option> entry : suggestion) {
            for (Option option : entry) {
                suggestionWords.add(manager
                        .text(option.getText().toString())
                        .highlight(analyzedWords)
                        .reverse()
                        .process());
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

    public List<String> analyze(String index, String text) {
        try {
            AnalyzeRequest request = AnalyzeRequest.withIndexAnalyzer(index, "ik_smart", text);
            List<AnalyzeToken> tokens = client.indices().analyze(request, RequestOptions.DEFAULT).getTokens();
            List<String> words = new ArrayList<>();
            int offset = 0;
            for (AnalyzeToken token : tokens) {
                if (token.getStartOffset() >= offset)
                    words.add(token.getTerm());
                offset = token.getEndOffset();
            }
            return words;
        }
        catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
