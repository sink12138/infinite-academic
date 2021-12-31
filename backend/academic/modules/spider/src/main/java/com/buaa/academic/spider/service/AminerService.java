package com.buaa.academic.spider.service;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Researcher;

import java.io.IOException;

public interface AminerService {

    Researcher findResearcher(String researcherId) throws IOException;

    Journal findJournal(String journalId) throws IOException;

    Institution findInstitution(String name);

}
