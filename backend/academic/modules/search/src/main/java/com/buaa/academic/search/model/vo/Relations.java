package com.buaa.academic.search.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Relations<I> {

    private int page;

    private int totalPages;

    private List<I> items = new ArrayList<>();

    public boolean hasMore() {
        return page < totalPages - 1;
    }

}
