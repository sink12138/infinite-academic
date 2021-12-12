package com.buaa.academic.spider.model.queueObject;

import com.buaa.academic.document.entity.Researcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResearcherSet {
    private String paperId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResearcherObject {
        private String url;
        private String name;
    }

    private ArrayList<ResearcherObject> researcherObjects;
}
