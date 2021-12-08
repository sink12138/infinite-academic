package com.buaa.academic.account.controller;

import com.buaa.academic.account.model.MessageIdList;
import com.buaa.academic.account.model.MessagePage;
import com.buaa.academic.account.model.MessageStatistic;
import com.buaa.academic.account.repository.MessageRepository;
import com.buaa.academic.document.system.Message;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ScriptType;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
@RequestMapping("/message")
@Api(tags = "用户消息接口", value = "/account/message")
public class MessageController {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/all")
    public Result<MessagePage> getMessages(@RequestHeader(value = "Auth") String userId,
                                           @RequestParam(value = "page") int page,
                                           @RequestParam(value = "size") int size,
                                           @RequestParam(value = "read") Boolean read) {
        QueryBuilder queryBuilder;
        if (read == null) {
            queryBuilder = QueryBuilders.termQuery("ownerId", userId);
        } else {
            queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("ownerId", userId))
                    .must(QueryBuilders.termQuery("read", read));
        }
        return new Result<MessagePage>().withData(searchMessage(queryBuilder, page, size));
    }

    @GetMapping("/statistic")
    public Result<MessageStatistic> messageStatistic(@RequestHeader(value = "Auth") String userId) {
        return new Result<MessageStatistic>().withData(statistic(userId));
    }

    @PostMapping("/read")
    public Result<Void> readMessage(@RequestHeader(value = "Auth") String userId,
                                    @RequestBody MessageIdList idList) {
        if (idList.getIdList().isEmpty()) {
            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("ownerId", userId))
                    .build();
            UpdateQuery query = UpdateQuery.builder(nativeSearchQuery)
                    .withScript("ctx._source.read=true")
                    .withScriptType(ScriptType.INLINE)
                    .build();
            template.updateByQuery(query, IndexCoordinates.of("message"));
            return new Result<>();
        }
        for (String id: idList.getIdList()) {
            UpdateQuery query = UpdateQuery.builder(id)
                    .withScript("ctx._source.read=true")
                    .build();
            template.update(query, IndexCoordinates.of("message"));
        }
        return new Result<>();
    }

    @PostMapping("/remove")
    public Result<Void> removeMessage(@RequestHeader(value = "Auth") String userId,
                                      @RequestBody MessageIdList idList) {
        NativeSearchQuery query;
        if (idList.getIdList().isEmpty()) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("ownerId", userId))
                    .build();
        } else  {
            query = new NativeSearchQueryBuilder()
                    .withIds(idList.getIdList())
                    .build();
        }
        template.delete(query, Message.class);
        return new Result<>();
    }

    private MessagePage searchMessage(QueryBuilder queryBuilder, int page, int size) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().
                withQuery(queryBuilder)
                .withSort(SortBuilders.fieldSort("time").order(SortOrder.DESC))
                .withPageable(PageRequest.of(page, size))
                .build();
        SearchHits<Message> searchHits = template.search(query, Message.class);
        List<Message> messages = new ArrayList<>();
        for (SearchHit<Message> searchHit: searchHits) {
            messages.add(searchHit.getContent());
        }
        long total = searchHits.getTotalHits();
        Integer pageCount = (int) (total + size - 1) / size;
        return new MessagePage(messages, pageCount);
    }

    private MessageStatistic statistic(String userId) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().
                withQuery(QueryBuilders.termQuery("ownerId", userId)).build();
        long messageCount = template.count(query, Message.class);
        NativeSearchQuery unreadQuery = new NativeSearchQueryBuilder()
                .withQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("ownerId", userId))
                                .must(QueryBuilders.termQuery("read", false))
                ).build();
        long unreadCount = template.count(unreadQuery, Message.class);
        return new MessageStatistic((int) messageCount, (int) unreadCount);
    }
}
