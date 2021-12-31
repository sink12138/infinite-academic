package com.buaa.academic.search.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchClientConfiguration {

    @Value("${spring.elasticsearch.rest.uris}")
    private String esUris;

    @Value("${spring.elasticsearch.rest.username}")
    private String username;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    @Bean
    public RestHighLevelClient getClient() {
        String[] uriComponents = esUris.split(":(//)?");
        HttpHost host = new HttpHost(uriComponents[1], Integer.parseInt(uriComponents[2]), uriComponents[0]);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder builder = RestClient.builder(host)
                .setRequestConfigCallback(
                        requestConfigBuilder -> {
                            requestConfigBuilder.setConnectTimeout(-1);
                            requestConfigBuilder.setSocketTimeout(-1);
                            requestConfigBuilder.setConnectionRequestTimeout(-1);
                            return requestConfigBuilder;
                        })
                .setHttpClientConfigCallback(
                        httpClientBuilder -> {
                            httpClientBuilder.disableAuthCaching();
                            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        });
        return new RestHighLevelClient(builder);
    }

}
