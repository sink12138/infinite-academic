package com.buaa.academic.account.controller;

import com.buaa.academic.account.model.MessageIdList;
import com.buaa.academic.account.model.MessagePage;
import com.buaa.academic.account.model.MessageStatistic;
import com.buaa.academic.account.repository.MessageRepository;
import com.buaa.academic.document.system.Message;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "用户消息处理接口", value = "/account/message")
public class MessageController {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private MessageRepository messageRepository;

    @ApiOperation(value = "查看所有消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "要查看第几页的消息"),
            @ApiImplicitParam(name = "size", value = "每一页的消息数量"),
            @ApiImplicitParam(name = "read", value = "是否只查看已读/未读消息，不传该参数默认查看所有消息，read为true查看所有已读消息，否则为未读")
    })
    @GetMapping("/all")
    public Result<MessagePage> getMessages(@RequestHeader(value = "Auth") String userId,
                                           @RequestParam(value = "page") int page,
                                           @RequestParam(value = "size") int size,
                                           @RequestParam(value = "read", required = false)  Boolean read) {
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
    @ApiOperation(value = "统计未读消息", notes = "用于显示一共有多少条消息未读")
    public Result<MessageStatistic> messageStatistic(@RequestHeader(value = "Auth") String userId) {
        return new Result<MessageStatistic>().withData(statistic(userId));
    }

    @PostMapping("/read")
    @ApiOperation(value = "标记已读", notes = "将idList中id对应的消息标记为已读，如果idList为空则将全部消息标为已读")
    @ApiImplicitParam(name = "idList", value = "要标为已读的消息的id列表，如果为空则表明要将全部消息标为已读")
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
    @ApiOperation(value = "删除消息", notes = "将idList中id对应的消息删除，idList为空则删除所有消息")
    @ApiImplicitParam(name = "idList", value = "要删除的消息的id列表，为空则删除所有消息")
    public Result<Void> removeMessage(@RequestHeader(value = "Auth") String userId,
                                      @RequestBody MessageIdList idList) {
        if (idList.getIdList().isEmpty()) {
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("ownerId", userId))
                    .build();
            template.delete(query, Message.class);
        } else  {
            for (String id: idList.getIdList())
                template.delete(id, Message.class);
        }
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
        NativeSearchQuery unreadQuery = new NativeSearchQueryBuilder()
                .withQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("ownerId", userId))
                                .must(QueryBuilders.termQuery("read", false))
                ).build();
        long unreadCount = template.count(unreadQuery, Message.class);
        return new MessageStatistic((int) unreadCount);
    }
}
