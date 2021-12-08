package com.buaa.academic.spider.model.queueObject;

import com.buaa.academic.document.entity.Institution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionObject {
    private String url;
    private Institution institution;
}
