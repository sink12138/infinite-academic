package com.buaa.academic.admin.config;

import com.buaa.academic.document.system.ApplicationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ConverterConfiguration {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addFormatters(@NonNull FormatterRegistry registry) {
                registry.addConverter(new ApplicationType.EnumToStringConverter());
                registry.addConverter(new ApplicationType.StringToEnumConverter());
            }
        };
    }

    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(List.of(
                new ApplicationType.EnumToStringConverter(),
                new ApplicationType.StringToEnumConverter()));
    }

    @Bean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext,
                                                         ElasticsearchCustomConversions customConversions) {
        MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(mappingContext);
        mappingElasticsearchConverter.setConversions(customConversions);
        return mappingElasticsearchConverter;
    }

}
