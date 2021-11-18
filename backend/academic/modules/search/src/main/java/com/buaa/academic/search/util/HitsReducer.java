package com.buaa.academic.search.util;

import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.item.PaperItem;
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
                if (manager.text(title).length() > 32)
                    title = manager.cut(32).process() + "...";
                item.setTitle(title);
            }
            if (hlt.containsKey("keywords")) {
                item.setKeywords(hlt.get("keywords"));
            }
            if (hlt.containsKey("abstract")) {
                String paperAbstract = hlt.get("abstract").get(0);
                if (manager.text(paperAbstract).length() > 80)
                    paperAbstract = manager.cut(80).process() + "...";
                item.setPaperAbstract(paperAbstract);
            }
            result.add(item);
        }
        return result;
    }

}
