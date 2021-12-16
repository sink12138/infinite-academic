package com.buaa.academic.spider.model.queueObject;

import com.buaa.academic.document.entity.Paper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperObject {

    private String url;

    private String paperId;

    private int depth;

}
