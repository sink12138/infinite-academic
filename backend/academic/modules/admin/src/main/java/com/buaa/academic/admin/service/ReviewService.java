package com.buaa.academic.admin.service;

import com.buaa.academic.model.application.PaperForApp;
import com.buaa.academic.model.application.PortalForApp;
import com.buaa.academic.model.application.Transfer;

public interface ReviewService {

    void modifyResearcher(String researcherId, PortalForApp portal);

    void savePaper(String paperIdIfExists, PaperForApp paper);

    void removePaper(String paperId);

    void transferPatent(String patentId, Transfer transfer);

}
