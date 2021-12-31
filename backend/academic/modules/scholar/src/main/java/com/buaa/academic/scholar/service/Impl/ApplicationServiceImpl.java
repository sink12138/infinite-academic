package com.buaa.academic.scholar.service.Impl;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.document.system.StatusType;
import com.buaa.academic.model.application.ApplicationInfo;
import com.buaa.academic.scholar.repository.ApplicationRepository;
import com.buaa.academic.scholar.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

@Service
public class ApplicationServiceImpl<T> implements ApplicationService<T> {
    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean submitAppWithCtf(ApplicationInfo<T> applicationInfo, String userId, ApplicationType type) {
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
        applicationRepository.save(application);
        appId = application.getId();
        redisTemplate.opsForValue().set(appId, applicationInfo.getContent());
        redisTemplate.expire(appId, Duration.ofDays(30));
        return true;
    }

    @Override
    public void submitAppWithoutCtf(ApplicationInfo<T> applicationInfo, String userId, ApplicationType type) {
        Application application = appBasicSetting(userId, applicationInfo.getEmail(), type);
        applicationRepository.save(application);
        String appId = application.getId();
        redisTemplate.opsForValue().set(appId, applicationInfo.getContent());
        redisTemplate.expire(appId, Duration.ofDays(30));
    }

    private Application appBasicSetting(String userId, String email, ApplicationType type) {
        Application application = new Application();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        application.setTime(simpleDateFormat.format(date));
        application.setStatus(StatusType.UNDER_REVIEW);
        application.setEmail(email);
        application.setUserId(userId);
        application.setType(type);
        return application;
    }
}
