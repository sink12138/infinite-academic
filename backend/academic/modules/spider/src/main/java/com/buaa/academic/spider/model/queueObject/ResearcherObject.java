package com.buaa.academic.spider.model.queueObject;

import com.buaa.academic.document.entity.Researcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResearcherObject {
    private String url;
    private Researcher researcher;
}
