package com.buaa.academic.admin.service;

import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.application.PaperForApp;
import com.buaa.academic.model.application.PortalForApp;
import com.buaa.academic.model.application.Transfer;

public interface ReviewService {

    void createResearcher(User user, PortalForApp portal);

    void modifyResearcher(String researcherId, PortalForApp portal);

    void mergeResearcher(String baseId, Iterable<String> otherIds);

    void savePaper(String paperIdIfExists, PaperForApp paper);

    void removePaper(String paperId);

    void transferPatent(String patentId, Transfer transfer);

}
