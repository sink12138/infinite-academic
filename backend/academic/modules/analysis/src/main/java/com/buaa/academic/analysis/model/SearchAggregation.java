package com.buaa.academic.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAggregation {
    public static class item {
        private String name;
        private Integer num;
    }
    private ArrayList<item> subjects;
    private ArrayList<item> topics;
    private ArrayList<item> authors;
    private ArrayList<item> institutions;
    private ArrayList<item> journals;
    private ArrayList<item> types;
    private ArrayList<item> keywords;
}
