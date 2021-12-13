package com.buaa.academic.spider.model.queueObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalObject {
    private String paperId;
    private String journalUrl;
}
