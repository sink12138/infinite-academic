package com.buaa.academic.admin.service.impl;

import com.buaa.academic.admin.dao.TrashRepository;
import com.buaa.academic.admin.service.ReviewService;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.system.Trash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Object REVIEW_LOCK = new Object();

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private TrashRepository trashRepository;

    @Override
    public void removePaper(String id) {
        new Thread(() -> {
            synchronized (REVIEW_LOCK) {
                Paper paper = template.get(id, Paper.class);
                if (paper == null)
                    return;
                Trash trash = new Trash();
                trash.setTitle(paper.getTitle());
                List<String> authors = new ArrayList<>();
                if (paper.getAuthors() != null) {
                    paper.getAuthors().forEach(author -> authors.add(author.getName()));
                    trash.setAuthors(authors);
                }
                trashRepository.save(trash);
                template.delete(id, Paper.class);
            }
        }, "remove-paper").start();
    }

}
