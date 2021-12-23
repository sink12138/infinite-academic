package com.buaa.academic.spider.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public abstract class StringUtil {

    public static String formatInstitutionName(String text) {
        if (text.isEmpty())
            return text;
        String[] parts = text
                .replaceAll("[?#%$*!@^`~]+", "")
                .replaceAll("\\s+\\.\\s+", ". ")
                .replaceAll("&", "and")
                .replaceAll("(?i)dept\\s*\\.\\s*", "Dept. ")
                .strip().split(";\\s*");
        if (parts.length > 0) {
            text = parts[0];
        }
        else {
            return text;
        }
        parts = text.split("\\s+");
        int len = parts.length;
        int start = text.matches("^[a-zA-Z].*") ? 2 : 1;
        for (int i = start; i < parts.length; ++i) {
            if (parts[i].matches(".*\\d{5,}.*")) {
                len = i;
                break;
            }
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 0; i < len; ++i) {
            joiner.add(parts[i]);
        }
        text = joiner.toString();
        if (text.matches("^[a-zA-Z].*")) {
            parts = text.split("\\s*[|,]\\s*");
            joiner = new StringJoiner(", ");
            for (String part : parts) {
                joiner.add(part);
            }
            text = joiner.toString();
            if (text.length() > 48) {
                joiner = new StringJoiner(", ");
                joiner.add(parts[0]);
                for (int i = 1; i < parts.length; ++i) {
                    if (!parts[i].matches(".*\\d{5,}.*")) {
                        joiner.add(parts[i]);
                        break;
                    }
                }
                text = joiner.toString();
            }
            return text;
        }
        else {
            Segment segment = HanLP.newSegment();
            text = text.replace(",", " ");
            text = text.replace("，", " ");
            parts = text.split("\\s+");
            ArrayList<String> partsNeeded = new ArrayList<>();
            for (String part : parts) {
                if (!part.matches(".*\\d{5,}$")) {
                    List<Term> terms = segment.seg(part);
                    boolean needed = false;
                    for (Term term : terms) {
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

}
