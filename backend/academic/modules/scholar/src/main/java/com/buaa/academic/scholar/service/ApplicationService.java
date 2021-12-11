package com.buaa.academic.scholar.service;

import com.buaa.academic.document.system.ApplicationType;
import com.buaa.academic.model.application.ApplicationInfo;

public interface ApplicationService<T> {

    Boolean submitAppWithCtf(ApplicationInfo<T> applicationInfo, String userId, ApplicationType type);

    void submitAppWithoutCtf(ApplicationInfo<T> applicationInfo, String userId, ApplicationType type);

}
