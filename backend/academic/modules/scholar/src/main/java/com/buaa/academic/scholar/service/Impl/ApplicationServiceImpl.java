package com.buaa.academic.scholar.service.Impl;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.scholar.repository.AppRepository;
import com.buaa.academic.scholar.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ApplicationServiceImpl<T> implements ApplicationService<T> {
    @Autowired
    AppRepository appRepository;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean submitApp(ApplicationInfo<T> applicationInfo, String userId, String type) {
        String appId;
        Application application = appBasicSetting(userId, applicationInfo.getContactEmail(), type);
        if (applicationInfo.getFileToken() != null) {
            application.setEmail(applicationInfo.getContactEmail());
            appRepository.save(application);
            appId = application.getId();
        } else if (applicationInfo.getWebsiteLink() != null) {
            application.setWebsiteLink(applicationInfo.getWebsiteLink());
            appRepository.save(application);
            appId = application.getId();
        } else
            return false;
        redisTemplate.opsForValue().set(appId, applicationInfo.getApplication());
        return true;
    }

    private Application appBasicSetting(String userId, String email, String type) {
        Application application = new Application();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        application.setTime(simpleDateFormat.format(date));
        application.setStatus("审核中");
        application.setEmail(email);
        application.setUserId(userId);
        application.setType(type);
        return application;
    }
}
