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
            if (hlt.containsKey("title") || hlt.containsKey("title.raw")) {
                List<String> highlights = hlt.get("title");
                if (highlights == null)
                    highlights = hlt.get("title.raw");
                String title = highlights.get(0);
                if (title == null)
                    title = hlt.get("title.raw").get(0);
                if (manager.text(title).length() > 128)
                    title = manager.cut(128).process() + "...";
                item.setTitle(title);
            }
            if (hlt.containsKey("keywords") || hlt.containsKey("keywords.raw")) {
                List<String> keywords = item.getKeywords();
                List<String> highlights = hlt.get("keywords");
                if (highlights == null)
                    highlights = hlt.get("keywords.raw");
                for (int i = 0, j = 0; i < highlights.size(); ++i) {
                    while (j < keywords.size() &&
                            !highlights.get(i).replaceAll(preTag, "").replaceAll(postTag, "").equals(keywords.get(j))) {
                        ++j;
                    }
                    if (j < keywords.size())
                        keywords.set(j, highlights.get(i));
                }
            }
            if (hlt.containsKey("paperAbstract")) {
                String paperAbstract = hlt.get("paperAbstract").get(0);
                if (manager.text(paperAbstract).length() > 256)
                    paperAbstract = manager.cut(256).process() + "...";
                item.setPaperAbstract(paperAbstract);
            }
            if (hlt.containsKey("authors.name")) {
                List<PaperItem.Author> authors = item.getAuthors();
                List<String> highlights = hlt.get("authors.name");
                for (int i = 0, j = 0; i < highlights.size(); ++i) {
                    while (j < authors.size() &&
                            !highlights.get(i).replaceAll(preTag, "").replaceAll(postTag, "").equals(authors.get(j).getName())) {
                        ++j;
                    }
                    if (j < authors.size())
                        authors.get(j).setName(highlights.get(i));
                }
            }
            if (hlt.containsKey("journal.title") || hlt.containsKey("journal.title.raw")) {
                List<String> highlights = hlt.get("journal.title");
                if (highlights == null)
                    highlights = hlt.get("journal.title.raw");
                item.getJournal().setTitle(highlights.get(0));
            }
            result.add(item);
        }
        return result;
    }

    public static List<ResearcherItem> reduceResearcherHits(SearchHits<Researcher> hits, String preTag, String postTag) {
        List<ResearcherItem> result = new ArrayList<>();
        for (SearchHit<Researcher> hit : hits) {
            ResearcherItem item = hit.getContent().reduce();
            Map<String, List<String>> hlt = hit.getHighlightFields();
            if (hlt.containsKey("name")) {
                item.setName(hlt.get("name").get(0));
            }
            if (hlt.containsKey("interests") || hlt.containsKey("interests.raw")) {
                List<String> interests = item.getInterests();
                List<String> highlights = hlt.get("interests");
                if (highlights == null)
                    highlights = hlt.get("interests.raw");
                for (int i = 0, j = 0; i < highlights.size(); ++i) {
                    while (j < interests.size() &&
                            !highlights.get(i).replaceAll(preTag, "").replaceAll(postTag, "").equals(interests.get(j))) {
                        ++j;
                    }
                    if (j < interests.size())
                        interests.set(j, highlights.get(i));
                }
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
            if (hlt.containsKey("title") || hlt.containsKey("title.raw")) {
                List<String> highlights = hlt.get("title");
                if (highlights == null)
                    highlights = hlt.get("title.raw");
                item.setTitle(highlights.get(0));
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
            if (hlt.containsKey("name") || hlt.containsKey("name.raw")) {
                List<String> highlights = hlt.get("name");
                if (highlights == null)
                    highlights = hlt.get("name.raw");
                item.setName(highlights.get(0));
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
            if (hlt.containsKey("title") || hlt.containsKey("title.raw")) {
                List<String> highlights = hlt.get("title");
                if (highlights == null)
                    highlights = hlt.get("title.raw");
                String title = highlights.get(0);
                if (manager.text(title).length() > 64)
                    title = manager.cut(64).process() + "...";
                item.setTitle(title);
            }
            result.add(item);
        }
        return result;
    }

}
