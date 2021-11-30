package com.buaa.academic.search.dao;

import java.util.HashMap;
import java.util.HashSet;

public class TextFields {

    private final static HashMap<String, HashSet<String>> textMappings = new HashMap<>() {{
        put("paper", new HashSet<>() {{
            add("title");
            add("institutions.name");
            add("abstract");
            add("keywords");
            add("subjects");
            add("topics");
        }});

        put("researcher", new HashSet<>() {{
            add("currentInst.name");
            add("institutions.name");
            add("interests");
        }});

        put("journal", new HashSet<>() {{
            add("title");
        }});

        put("institution", new HashSet<>() {{
            add("name");
        }});

        put("patent", new HashSet<>() {{
            add("title");
            add("applicant");
            add("abstract");
        }});
    }};

    public static boolean contains(String indexName, String fieldName) {
        if (!textMappings.containsKey(indexName))
            throw new IllegalArgumentException("No index named " + indexName);
        return textMappings.get(indexName).contains(fieldName);
    }

}
