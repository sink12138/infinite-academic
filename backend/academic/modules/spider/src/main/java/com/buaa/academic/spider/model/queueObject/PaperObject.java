package com.buaa.academic.spider.model.queueObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperObject implements Comparable<PaperObject> {

    private String url;

    private String paperId;

    private int depth;

    @Override
    public int compareTo(@NonNull PaperObject another) {
        return another.depth - this.depth; // Desc
    }

}
