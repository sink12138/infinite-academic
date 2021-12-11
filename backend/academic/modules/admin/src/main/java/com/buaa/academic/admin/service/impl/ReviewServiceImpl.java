package com.buaa.academic.admin.service.impl;

import com.buaa.academic.admin.service.ReviewService;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.system.Trash;
import com.buaa.academic.model.application.PortalForApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Override
    public void removePaper(String id) {
        new Thread(() -> {
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
            template.save(trash);
            template.delete(id, Paper.class);
        }, "remove-paper").start();
    }

    @Override
    public void modifyResearcher(String researcherId, PortalForApp portal) {
        new Thread(() -> {
            Researcher researcher = template.get(researcherId, Researcher.class);
            Objects.requireNonNull(researcher);
            /* name */
            researcher.setName(portal.getName());
            /* currentInst */
            PortalForApp.Institution portalCurInst = portal.getCurrentInst();
            String curInstId = portalCurInst.getId();
            if (curInstId != null) {
                Institution curInst = template.get(curInstId, Institution.class);
                Objects.requireNonNull(curInst);
                Researcher.Institution researcherCurInst = new Researcher.Institution();
                researcherCurInst.setId(curInstId);
                researcherCurInst.setName(curInst.getName());
                researcher.setCurrentInst(researcherCurInst);
            }
            else {
                Institution curInst = new Institution();
                curInst.setName(portalCurInst.getName());
                template.save(curInst);
                Researcher.Institution researcherCurInst = new Researcher.Institution();
                researcherCurInst.setId(curInst.getId());
                researcherCurInst.setName(portalCurInst.getName());
                researcher.setCurrentInst(researcherCurInst);
            }
            /* institutions */
            if (portal.getInstitutions() != null) {
                List<Researcher.Institution> institutions = new ArrayList<>();
                portal.getInstitutions().forEach(portalInst -> {
                    String institutionId = portalInst.getId();
                    if (institutionId != null) {
                        Institution institution = template.get(institutionId, Institution.class);
                        Objects.requireNonNull(institution);
                        Researcher.Institution researcherInst = new Researcher.Institution();
                        researcherInst.setId(institutionId);
                        researcherInst.setName(institution.getName());
                        institutions.add(researcherInst);
                    }
                    else {
                        Institution institution = new Institution();
                        institution.setName(portalInst.getName());
                        template.save(institution);
                        Researcher.Institution researcherInst = new Researcher.Institution();
                        researcherInst.setId(institution.getId());
                        researcherInst.setName(portalInst.getName());
                        institutions.add(researcherInst);
                    }
                });
                researcher.setInstitutions(institutions);
            }
            /* hIndex, gIndex, interests */
            researcher.setHIndex(portal.getHIndex());
            researcher.setGIndex(portal.getGIndex());
            researcher.setInterests(portal.getInterests());
            template.save(researcher);
        }, "modify-portal").start();
    }

}
