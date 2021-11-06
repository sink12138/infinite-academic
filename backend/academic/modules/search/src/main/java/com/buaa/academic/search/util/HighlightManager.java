package com.buaa.academic.search.util;

public class HighlightManager {

    private final String[] tags = new String[2];

    public HighlightManager(String preTag, String postTag) {
        tags[0] = preTag;
        tags[1] = postTag;
    }

    public int length(String text) {
        return text.replaceAll(tags[0], "").replaceAll(tags[1], "").length();
    }

    public String cut(String text, int endOffset) {
        return cut(text, 0, endOffset);
    }

    public String cut(String text, int beginOffset, int endOffset) {
        StringBuilder builder = new StringBuilder();
        String[] sections = text.split(String.format("(%s)|(%s)", tags[0], tags[1]));
        boolean between = false;
        int index = 0;
        int offset = 0;
        for (; offset < beginOffset && index < sections.length; between = !between, ++index) {
            if (offset + sections[index].length() > beginOffset) {
                sections[index] = sections[index].substring(beginOffset - offset);
                offset = beginOffset;
                break;
            }
            offset += sections[index].length();
        }
        if (index == sections.length)
            throw new StringIndexOutOfBoundsException("begin " + beginOffset + ", end " + endOffset + ", length " + length(text));
        if (between && offset < endOffset)
            builder.append(tags[0]);
        for (; offset < endOffset && index < sections.length; between = !between, ++index) {
            if (offset + sections[index].length() > endOffset) {
                builder.append(sections[index], 0, endOffset - offset);
                if (between)
                    builder.append(tags[1]);
                break;
            }
            builder.append(sections[index]);
            offset += sections[index].length();
            if (between)
                builder.append(tags[1]);
            else if (offset < endOffset)
                builder.append(tags[0]);
        }
        if (index == sections.length && offset < endOffset)
            throw new StringIndexOutOfBoundsException("begin " + beginOffset + ", end " + endOffset + ", length " + length(text));
        return builder.toString();
    }

}
