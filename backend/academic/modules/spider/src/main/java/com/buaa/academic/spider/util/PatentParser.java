package com.buaa.academic.spider.util;

import com.buaa.academic.document.entity.Patent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatentParser {
    private String url;
    private Patent patent;

    public void spider(){

    }
}
