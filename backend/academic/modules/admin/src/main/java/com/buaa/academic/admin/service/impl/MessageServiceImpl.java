package com.buaa.academic.admin.service.impl;

import com.buaa.academic.admin.service.MessageService;
import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public void sendRejectMessage(Application application, String reason) {
        Message message = new Message(
                null,
                application.getUserId(),
                application.getType().getDescription() + "被拒绝",
                String.format("很遗憾，您于 %s 提交的 %s 申请未通过管理员审核，原因：%s\n您可以再次提交申请，或联系网站管理员。",
                        application.getTime(),
                        application.getType().getDescription(),
                        reason),
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),
                false);
        template.save(message);
    }

    @Override
    public void sendAcceptMessage(Application application) {
        Message message = new Message(
                null,
                application.getUserId(),
                application.getType().getDescription() + "申请通过",
                String.format("恭喜！您于 %s 提交的 %s 申请已通过管理员审核。数据更新可能略有延迟，请稍候。",
                        application.getTime(),
                        application.getType().getDescription()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),
                false);
        template.save(message);
    }

}
