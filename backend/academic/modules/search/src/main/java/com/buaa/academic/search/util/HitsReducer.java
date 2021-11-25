package com.buaa.academic.search.util;

import com.buaa.academic.document.entity.*;
import com.buaa.academic.document.entity.item.*;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HitsReducer {

    public static List<PaperItem> reducePaperHits(SearchHits<Paper> hits, String preTag, String postTag) {
        List<PaperItem> result = new ArrayList<>();
        HighlightManager manager = new HighlightManager(preTag, postTag);
        for (SearchHit<Paper> hit : hits) {
            PaperItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("title")) {
                String title = hlt.get("title").get(0);
                if (manager.text(title).length() > 64)
                    title = manager.cut(64).process() + "...";
                item.setTitle(title);
            }
            if (hlt.containsKey("keywords")) {
                item.setKeywords(hlt.get("keywords"));
            }
            if (hlt.containsKey("abstract")) {
                String paperAbstract = hlt.get("abstract").get(0);
                if (manager.text(paperAbstract).length() > 128)
                    paperAbstract = manager.cut(128).process() + "...";
                item.setPaperAbstract(paperAbstract);
            }
            result.add(item);
        }
        return result;
    }

    public static List<ResearcherItem> reduceResearcherHits(SearchHits<Researcher> hits) {
        List<ResearcherItem> result = new ArrayList<>();
        for (SearchHit<Researcher> hit : hits) {
            ResearcherItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("name")) {
                item.setName(hlt.get("name").get(0));
            }
            if (hlt.containsKey("interests")) {
                item.setInterests(hlt.get("interests"));
            }
            result.add(item);
        }
        return result;
    }

    public static List<JournalItem> reduceJournalHits(SearchHits<Journal> hits) {
        List<JournalItem> result = new ArrayList<>();
        for (SearchHit<Journal> hit : hits) {
            JournalItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("title")) {
                item.setTitle(hlt.get("title").get(0));
            }
            result.add(item);
        }
        return result;
    }

    public static List<InstitutionItem> reduceInstitutionHits(SearchHits<Institution> hits) {
        List<InstitutionItem> result = new ArrayList<>();
        for (SearchHit<Institution> hit : hits) {
            InstitutionItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("name")) {
                item.setName(hlt.get("name").get(0));
            }
            result.add(item);
        }
        return result;
    }

    public static List<PatentItem> reducePatentHits(SearchHits<Patent> hits, String preTag, String postTag) {
        List<PatentItem> result = new ArrayList<>();
        HighlightManager manager = new HighlightManager(preTag, postTag);
        for (SearchHit<Patent> hit : hits) {
            PatentItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("title")) {
                String title = hlt.get("title").get(0);
                if (manager.text(title).length() > 64)
                    title = manager.cut(64).process() + "...";
                item.setTitle(title);
            }
            result.add(item);
        }
        return result;
    }

}
