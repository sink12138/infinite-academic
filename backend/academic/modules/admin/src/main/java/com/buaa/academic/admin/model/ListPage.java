package com.buaa.academic.admin.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListPage<T> {

    @ApiModelProperty(value = "当前页上的所有申请项目")
    private List<T> items = new ArrayList<>();

    @ApiModelProperty(value = "总页数")
    private int pageCount;

    public void add(T item) {
        this.items.add(item);
    }

}
