package com.buaa.academic.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cooperation {
    private String id;
    private String name;
    private Integer times;
}
