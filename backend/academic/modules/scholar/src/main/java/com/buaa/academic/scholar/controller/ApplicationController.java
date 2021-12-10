package com.buaa.academic.scholar.controller;

import com.buaa.academic.document.system.Application;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.scholar.model.ApplicationPage;
import com.buaa.academic.scholar.repository.AppRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/app")
@Validated
@Api(tags = "申请相关")
public class ApplicationController {
    @Autowired
    AppRepository appRepository;

    @GetMapping("/list")
    public Result<ApplicationPage> getAllApp(@RequestHeader(value = "Auth") String userId,
                                             @RequestParam(value = "page") Integer page,
                                             @RequestParam(value = "size") Integer size,
                                             @RequestParam(value = "type", required = false) String type,
                                             @RequestParam(value = "status", required = false) String status) {
        Result<ApplicationPage> result = new Result<>();
        ApplicationPage applicationPage = new ApplicationPage();
        SearchPage<Application> applicationSearchPage;
        Pageable pageable = PageRequest.of(page, size);
        if (type == null && status == null)
            applicationSearchPage = appRepository.findByUserIdEqualsOrderByTimeDesc(userId, pageable);
        else if (type != null && status != null)
            applicationSearchPage = appRepository.findByUserIdEqualsAndStatusEqualsAndTypeEqualsOrderByTimeDesc(userId, status, type, pageable);
        else if (type != null)
            applicationSearchPage = appRepository.findByUserIdEqualsAndTypeEqualsOrderByTimeDesc(userId, type, pageable);
        else
            applicationSearchPage = appRepository.findByUserIdEqualsAndStatusEqualsOrderByTimeDesc(userId, status, pageable);
        applicationPage.setPageCount(applicationSearchPage.getTotalPages());
        ArrayList<Application> applications = new ArrayList<>();
        applicationSearchPage.getSearchHits().forEach(applicationSearchHit ->
                applications.add(applicationSearchHit.getContent())
        );
        applicationPage.setApplications(applications);
        return result.withData(applicationPage);
    }
}
