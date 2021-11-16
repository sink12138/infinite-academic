package com.buaa.academic.search.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrollPage<I> {

    private boolean hasMore;

    private List<I> items;

}
