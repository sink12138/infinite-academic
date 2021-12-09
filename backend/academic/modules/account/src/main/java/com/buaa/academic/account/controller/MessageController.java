package com.buaa.academic.account.controller;

import com.buaa.academic.account.model.MessageIdList;
import com.buaa.academic.account.model.MessagePage;
import com.buaa.academic.account.repository.MessageRepository;
import com.buaa.academic.document.system.Message;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.index.query.QueryBuilders;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
@RequestMapping("/message")
@Api(tags = "用户消息提醒")
public class MessageController {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/list")
    @ApiOperation(value = "查看所有消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "要查看第几页的消息，从0开始"),
            @ApiImplicitParam(name = "size", value = "每一页的消息数量，最大为30"),
            @ApiImplicitParam(name = "read", value = "是否只查看已读/未读消息，不传该参数默认查看所有消息，read为true查看所有已读消息，否则为未读")})
    public Result<MessagePage> listMessages(@RequestHeader(value = "Auth") String userId,
                                            @RequestParam(value = "page") @PositiveOrZero int page,
                                            @RequestParam(value = "size") @Range(min = 1, max = 30) int size,
                                            @RequestParam(value = "read", required = false) Boolean read) {
        Result<MessagePage> result = new Result<>();
        MessagePage messagePage = new MessagePage();
        SearchPage<Message> searchPage;
        if (read == null) {
            searchPage = messageRepository.findByOwnerIdOrderByTimeDesc(userId, PageRequest.of(page, size));
        } else {
            searchPage = messageRepository.findByOwnerIdAndReadOrderByTimeDesc(userId, read, PageRequest.of(page, size));
        }
        messagePage.setTotalPages(searchPage.getTotalPages());
        List<Message> messages = new ArrayList<>();
        searchPage.forEach(hit -> messages.add(hit.getContent()));
        messagePage.setMessages(messages);
        return result.withData(messagePage);
    }

    @GetMapping("/count")
    @ApiOperation(value = "统计未读消息", notes = "用于显示一共有多少条消息未读")
    public Result<Long> countMessages(@RequestHeader(value = "Auth") String userId) {
        return new Result<Long>().withData(messageRepository.countByOwnerIdAndRead(userId, false));
    }

    @PostMapping("/read")
    @ApiOperation(value = "标记已读", notes = "将idList中id对应的消息标记为已读，如果idList为空则将全部消息标为已读")
    @ApiImplicitParam(name = "idList", value = "要标为已读的消息的id列表，如果为空则表明要将全部消息标为已读")
    public Result<Void> readMessages(@RequestHeader(value = "Auth") String userId,
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
        }
        else for (String id: idList.getIdList()) {
            Message message = template.get(id, Message.class);
            if (message == null || !message.getOwnerId().equals(userId))
                continue;
            message.setRead(true);
            template.save(message);
        }
        return new Result<>();
    }

    @PostMapping("/remove")
    @ApiOperation(value = "删除消息", notes = "将idList中id对应的消息删除，idList为空则删除所有消息")
    @ApiImplicitParam(name = "idList", value = "要删除的消息的id列表，为空则删除所有消息")
    public Result<Void> removeMessage(@RequestHeader(value = "Auth") String userId,
                                      @RequestBody MessageIdList idList) {
        if (idList.getIdList().isEmpty())
            messageRepository.deleteByOwnerId(userId);
        else for (String id : idList.getIdList()) {
            Message message = template.get(id, Message.class);
            if (message == null || !message.getOwnerId().equals(userId))
                continue;
            messageRepository.deleteById(id);
        }
        return new Result<>();
    }

}
