package com.buaa.academic.admin.service;

import com.buaa.academic.model.application.PortalForApp;

public interface ReviewService {

    void modifyResearcher(String researcherId, PortalForApp portal);

    void removePaper(String id);

}
