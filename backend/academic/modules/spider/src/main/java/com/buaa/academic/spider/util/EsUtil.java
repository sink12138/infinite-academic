package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;

import java.util.List;

public interface EsUtil {

    Paper findPaperByTileAndAuthors(String title, List<Paper.Author> authors);

    Paper findPaperById(String id);

    Researcher findResearcherByNameAndInst(String name, String inst);

    Institution findInstByName(String name);

    Journal findJournalByName(String name);

    boolean inTrash(String title, List<Paper.Author> authors);

}
