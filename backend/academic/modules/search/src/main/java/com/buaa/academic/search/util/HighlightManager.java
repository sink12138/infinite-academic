package com.buaa.academic.search.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HighlightManager {

    private final String[] tags = new String[2];

    private int[] range;
    
    private String text;
    
    private boolean between;

    public HighlightManager(String preTag, String postTag) {
        tags[0] = preTag;
        tags[1] = postTag;
    }
    
    public HighlightManager text(String text) {
        this.text = text;
        return this.optimize();
    }

    public HighlightManager optimize() {
        this.text = this.text
                .replaceAll(String.format("%s%s", tags[0], tags[1]), "")
                .replaceAll(String.format("%s%s", tags[1], tags[0]), "");
        return this;
    }

    public int length() {
        if (this.text == null)
            throw new NullPointerException("No input text");
        return length(this.text);
    }

    public int length(String text) {
        if (text == null)
            throw new IllegalArgumentException("text cannot be null");
        return text.replaceAll(tags[0], "").replaceAll(tags[1], "").length();
    }

    public HighlightManager cut(int beginOffset, int endOffset) {
        this.range = new int[] { beginOffset, endOffset };
        return this;
    }
    
    public HighlightManager cut(int endOffset) {
        return cut(0, endOffset);
    }
    
    public HighlightManager reverse() {
        this.between = !this.between;
        return this;
    }
    
    public String process() {
        if (this.text == null)
            throw new IllegalStateException("No input text");
        String[] sections = text.split(String.format("(%s)|(%s)", tags[0], tags[1]));
        if (this.range == null)
            this.range = new int[] { 0, length(text) };
        StringBuilder builder = new StringBuilder();
        boolean between = this.between;
        int index = 0;
        int offset = 0;
        for (; offset < this.range[0] && index < sections.length; between = !between, ++index) {
            if (offset + sections[index].length() > this.range[0]) {
                sections[index] = sections[index].substring(this.range[0] - offset);
                offset = this.range[0];
                break;
            }
            offset += sections[index].length();
        }
        if (index == sections.length)
            throw new StringIndexOutOfBoundsException("begin " + this.range[0] + ", end " + this.range[1] + ", length " + length());
        if (between && offset < this.range[1])
            builder.append(tags[0]);
        for (; offset < this.range[1] && index < sections.length; between = !between, ++index) {
            if (offset + sections[index].length() > this.range[1]) {
                builder.append(sections[index], 0, this.range[1] - offset);
                if (between)
                    builder.append(tags[1]);
                break;
            }
            builder.append(sections[index]);
            offset += sections[index].length();
            if (between)
                builder.append(tags[1]);
            else if (offset < this.range[1])
                builder.append(tags[0]);
        }
        if (index == sections.length && offset < this.range[1])
            throw new StringIndexOutOfBoundsException("begin " + this.range[0] + ", end " + this.range[1] + ", length " + length());
        return builder.toString();
    }

    public HighlightManager highlight(List<String> reference) {
        if (reference == null)
            throw new IllegalArgumentException("reference cannot be null");
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        for (String word : reference) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(this.text);
            if (matcher.find(offset)) {
                int start = matcher.start(0);
                if (start > offset) {
                    builder.append(this.text, offset, start);
                }
                builder.append(tags[0]).append(matcher.group(0)).append(tags[1]);
                offset = matcher.end(0);
            }
        }
        builder.append(this.text, offset, this.text.length());
        this.text = builder.toString();
        return this.optimize();
    }

}
