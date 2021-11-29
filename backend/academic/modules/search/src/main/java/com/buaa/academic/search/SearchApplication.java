package com.buaa.academic.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.session.FlushMode;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE, maxInactiveIntervalInSeconds = 60)
public class SearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

}
