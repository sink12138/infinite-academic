package com.buaa.academic.admin.config;

import com.buaa.academic.document.system.ApplicationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

@Configuration
public class ConverterConfiguration {

    @Bean
    public ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        defaultConversionService.addConverter(new ApplicationType.EnumToStringConverter());
        defaultConversionService.addConverter(new ApplicationType.StringToEnumConverter());
        return new MappingElasticsearchConverter(mappingContext, defaultConversionService);
    }

}
