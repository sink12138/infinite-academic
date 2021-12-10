package com.buaa.academic.scholar.service;

import com.buaa.academic.model.application.ApplicationInfo;

public interface ApplicationService<T> {
    Boolean submitApp(ApplicationInfo<T> applicationInfo, String userId, String type);
}
