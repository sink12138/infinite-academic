package com.buaa.academic.analysis.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "实体频次统计")
public class EntityFrequency extends Frequency {

    @ApiModelProperty(value = "实体id")
    private String id;

    public EntityFrequency(String name, int docCount, String id) {
        super(name, docCount);
        this.id = id;
    }

}
