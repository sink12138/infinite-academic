package com.buaa.academic.spider.service;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;

import java.util.List;

public interface ExistenceService {
    Paper findPaperByTileAndAuthors(String title, List<Paper.Author> authors);
    Paper findPaperById(String id);
    Researcher findResearcherById(String id);
    Institution findInstByName(String name);
    Journal findJournalByName(String name);
}
