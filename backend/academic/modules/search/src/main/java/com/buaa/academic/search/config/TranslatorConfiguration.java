package com.buaa.academic.search.config;

import com.buaa.academic.tool.translator.Translator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TranslatorConfiguration implements ApplicationRunner {

    @Value("${translator.app-id}")
    private String appId;

    @Value("${translator.security-key}")
    private String securityKey;

    @Override
    public void run(ApplicationArguments args) {
        Translator.appid = appId;
        Translator.securityKey = securityKey;
    }

}
