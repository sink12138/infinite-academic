package com.buaa.academic.tool.util;

public class StringUtils {

    public static String cut(String text, int maxLength) {
        if (text == null)
            return null;
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    public static String strip(String text) {
        if (text == null)
            return null;
        return text.trim().replaceAll("\\s+", " ");
    }

    public static String strip(String text, int maxLength) {
        if (text == null)
            return null;
        return cut(strip(text), maxLength);
    }

}
