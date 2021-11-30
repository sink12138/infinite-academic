package com.buaa.academic.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HighlightConfiguration {

    @Value("${spring.elasticsearch.highlight.pre-tag}")
    private String preTag;

    @Value("${spring.elasticsearch.highlight.post-tag}")
    private String postTag;

    public String preTag() {
        return preTag;
    }

    public String postTag() {
        return postTag;
    }

}
