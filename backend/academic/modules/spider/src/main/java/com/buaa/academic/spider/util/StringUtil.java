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
                .replaceAll("(?i)univ\\s*\\.", "University")
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
            if (text.length() > 32) {
                joiner = new StringJoiner(", ");
                joiner.add(parts[0]);
                for (int i = 1; i < parts.length; ++i) {
                    if (!parts[i].matches(".*\\d{5,}.*")) {
                        joiner.add(parts[i]);
                        break;
                    }
                }
                text = joiner.toString();
                if (text.contains("(")) {
                    parts = text.split("\\s*\\(\\s*");
                    if (parts.length <= 1)
                        return text;
                    if (parts[1].equals(parts[0])) {
                        text = parts[0];
                    }
                    else if (parts[1].startsWith(parts[0])) {
                        text = parts[0];
                        String[] par = parts[1].split("\\s*\\)\\s*", 2);
                        String remain = par[0].substring(parts[0].length()).replaceAll("^\\s*,\\s*", "").strip();
                        if (remain.startsWith(parts[0]))
                            text = remain;
                        if (par.length > 1)
                            text += par[1];
                    }
                }
            }
            parts = text.split(",\\s*");
            if (parts.length > 1 && parts[0].equals(parts[1])) {
                text = parts[0];
            }
            int index = text.toLowerCase().indexOf("university");
            if (index > 3 && text.length() > index + 10 && text.charAt(index + 10) == ',') {
                text = text.substring(0, index + 10);
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

    public static void main(String[] args) {
        System.out.println(StringUtil.formatInstitutionName("Peking University(Peking University), Beijing"));
    }

}
