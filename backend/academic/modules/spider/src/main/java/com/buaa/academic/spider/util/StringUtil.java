package com.buaa.academic.spider.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import java.util.ArrayList;
import java.util.List;

public abstract class StringUtil {
    public static String rmPlaceNameAndCode(String text) {
        if (text.matches("^[a-zA-Z].*"))
            return text;
        Segment segment = HanLP.newSegment();
        text = text.replace(",", " ");
        text = text.replace("，", " ");
        String[] parts = text.split("\\s+");
        ArrayList<String> partsNeeded = new ArrayList<>();
        for (String part: parts) {
            if (!part.matches(".*\\d{6}$")) {
                List<Term> terms = segment.seg(part);
                boolean needed = false;
                for (Term term: terms) {
                    if (!term.toString().substring(term.length() + 1).equals("ns"))
                        needed = true;
                }
                if (needed) {
                    partsNeeded.add(part);
                }
            }
        }
        return String.join(" ", partsNeeded);
    }
}
