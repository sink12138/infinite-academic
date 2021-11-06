package com.buaa.academic.search.service;

import com.buaa.academic.document.entity.Paper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface SearchService {

    SearchHits<Paper> smartSearchForPapers(String keyword, Pageable pageable);

}
