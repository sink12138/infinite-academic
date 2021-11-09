package com.buaa.academic.search.util;

public class HighlightManager {

    private final String[] tags = new String[2];
    
    private String[] sections;
    
    private int[] range;
    
    private String text;
    
    private boolean between;

    public HighlightManager(String preTag, String postTag) {
        tags[0] = preTag;
        tags[1] = postTag;
    }
    
    public HighlightManager text(String text) {
        this.text = text;
        this.sections = text.split(String.format("(%s)|(%s)", tags[0], tags[1]));
        return this;
    }

    public int length(String text) {
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
            throw new NullPointerException("No input text");
        if (this.range == null)
            this.range = new int[] { 0, length(text) };
        StringBuilder builder = new StringBuilder();
        boolean between = this.between;
        int index = 0;
        int offset = 0;
        for (; offset < this.range[0] && index < this.sections.length; between = !between, ++index) {
            if (offset + sections[index].length() > this.range[0]) {
                sections[index] = sections[index].substring(this.range[0] - offset);
                offset = this.range[0];
                break;
            }
            offset += sections[index].length();
        }
        if (index == sections.length)
            throw new StringIndexOutOfBoundsException("begin " + this.range[0] + ", end " + this.range[1] + ", length " + length(text));
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
            throw new StringIndexOutOfBoundsException("begin " + this.range[0] + ", end " + this.range[1] + ", length " + length(text));
        return builder.toString();
    }

}
