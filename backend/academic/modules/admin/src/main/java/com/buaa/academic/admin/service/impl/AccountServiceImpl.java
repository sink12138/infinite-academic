package com.buaa.academic.admin.service.impl;

import com.buaa.academic.admin.client.FeignOperation;
import com.buaa.academic.admin.client.ResourceClient;
import com.buaa.academic.admin.dao.MessageRepository;
import com.buaa.academic.admin.service.AccountService;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.model.web.Result;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ResourceClient resourceClient;

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Override
    public void removeUser(String userId) {
        new Thread(() -> {
            /* user */
            elasticTemplate.delete(userId, User.class);
            /* messages */
            messageRepository.deleteByOwnerId(userId);
            /* applications */
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("userId", userId))
                    .withFields("id", "fileToken")
                    .withPageable(PageRequest.of(0, 50))
                    .build();
            for (SearchScrollHits<Application> searchHits = elasticTemplate.searchScrollStart(1000, searchQuery, Application.class, IndexCoordinates.of("message"));
                 searchHits.hasSearchHits();
                 searchHits = elasticTemplate.searchScrollContinue(searchHits.getScrollId(), 1000, Application.class, IndexCoordinates.of("application"))) {
                searchHits.forEach(hit -> {
                    String fileToken = hit.getContent().getFileToken();
                    if (fileToken != null) {
                        new FeignOperation<Void>("remove-file") {
                            @Override
                            public Result<Void> apply() {
                                return resourceClient.remove(fileToken);
                            }
                        }.start();
                    }
                    elasticTemplate.delete(hit.getContent().getId(), Application.class);
                    redisTemplate.delete(hit.getContent().getId());
                });
            }
            log.info("Successfully removed user {}", userId);

        }, "remove-user").start();
    }

}
