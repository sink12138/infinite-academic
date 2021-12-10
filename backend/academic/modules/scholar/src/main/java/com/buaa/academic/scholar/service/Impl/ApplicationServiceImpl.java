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
    public Boolean submitAppWithCtf(ApplicationInfo<T> applicationInfo, String userId, String type) {
        String appId;
        Application application = appBasicSetting(userId, applicationInfo.getEmail(), type);
        if (applicationInfo.getFileToken() == null && applicationInfo.getWebsiteLink() == null)
            return false;
        if (applicationInfo.getFileToken() != null) {
            application.setFileToken(applicationInfo.getFileToken());
        }
        if (applicationInfo.getWebsiteLink() != null) {
            application.setWebsiteLink(applicationInfo.getWebsiteLink());
        }
        appRepository.save(application);
        appId = application.getId();
        redisTemplate.opsForValue().set(appId, applicationInfo.getApplication());
        return true;
    }

    @Override
    public void submitAppWithoutCtf(ApplicationInfo<T> applicationInfo, String userId, String type) {
        Application application = appBasicSetting(userId, applicationInfo.getEmail(), type);
        appRepository.save(application);
        redisTemplate.opsForValue().set(application.getId(), applicationInfo.getApplication());
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
