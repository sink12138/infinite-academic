package com.buaa.academic.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAggregation {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class item {
        private String name;
        private Integer num;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class aggregationTerm {
        private String term;
        private ArrayList<item> items;
    }

    private ArrayList<aggregationTerm> aggregationTerms;
}
