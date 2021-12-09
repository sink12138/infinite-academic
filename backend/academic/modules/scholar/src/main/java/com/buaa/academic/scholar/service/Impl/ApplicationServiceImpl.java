package com.buaa.academic.scholar.service.Impl;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.scholar.repository.AppRepository;
import com.buaa.academic.scholar.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    AppRepository appRepository;

    @Override
    public String saveAppWithFile(String userId, String email, String fileToken, String type) {
        Application application = setAppBasic(userId, email, type);
        application.setFileToken(fileToken);
        appRepository.save(application);
        return application.getId();
    }

    @Override
    public String saveAppWithLink(String userId, String email, String websiteLink, String type) {
        Application application = setAppBasic(userId, email, type);
        application.setWebsiteLink(websiteLink);
        appRepository.save(application);
        return application.getId();
    }


    private Application setAppBasic(String userId, String email, String type) {
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
