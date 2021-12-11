package com.buaa.academic.scholar.utils;

import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Paper;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.model.application.*;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

@Component
public  class ExistenceCheck {
    @Autowired
    ElasticsearchRestTemplate template;

    public Boolean certificationCheck(Certification certification) {
        if (certification.getCreate() == null)
            return (certification.getClaims() != null && this.claimCheck(certification.getClaims()));
        else if (certification.getClaims() == null)
            return certification.getCreate() != null && this.portalForCheck(certification.getCreate());
        else
            return false;
    }

    public Boolean claimCheck(Claim claim) {
        for (String id: claim.getPortals()) {
            if (!template.exists(id, Researcher.class))
                return false;
        }
        return true;
    }

    public Boolean modificationCheck(Modification modification) {
        return this.portalForCheck(modification.getInfo());
    }

    public Boolean paperAddCheck(PaperAdd paperAdd) {
        return this.paperForAppCheck(paperAdd.getAdd());
    }

    public Boolean paperEditCheck(PaperEdit paperEdit) {
        if (!template.exists(paperEdit.getPaperId(), Paper.class))
            return false;
        return this.paperForAppCheck(paperEdit.getEdit());
    }

    public Boolean paperRemoveCheck(PaperRemove paperRemove) {
        return template.exists(paperRemove.getPaperId(), Paper.class);
    }

    public Boolean paperForAppCheck(PaperForApp paperForApp) {
        for (PaperForApp.Author author: paperForApp.getAuthors()) {
            if (author.getId() != null) {
                if (!template.exists(author.getId(), Researcher.class))
                    return false;
                author.setName(null);
                author.setInstName(null);
            } else {
                if(author.getName() == null)
                    return false;
            }
        }
        for (PaperForApp.Institution institution: paperForApp.getInstitutions()) {
            if (institution.getId() != null) {
                if (!template.exists(institution.getId(), Institution.class))
                    return false;
                institution.setName(null);
            } else {
                if (institution.getName() == null)
                    return false;
            }
        }
        if (paperForApp.getJournal() != null) {
            if (paperForApp.getJournal().getId() != null) {
                if (!template.exists(paperForApp.getJournal().getId(), Journal.class))
                    return false;
                PaperForApp.Journal journalOnlyId = new PaperForApp.Journal();
                journalOnlyId.setId(paperForApp.getJournal().getId());
                paperForApp.setJournal(journalOnlyId);
            } else {
                if (paperForApp.getJournal().getTitle() == null)
                    return false;
            }
        }
        if (paperForApp.getReferencePapers() != null) {
            for (PaperForApp.ReferencePaper reference : paperForApp.getReferencePapers()) {
                if (reference.getId() != null) {
                    if (!template.exists(reference.getId(), Paper.class))
                        return false;
                    reference.setTitle(null);
                } else {
                    if (reference.getTitle() == null)
                        return false;
                }
            }
        }
        return true;
    }

    public Boolean portalForCheck(PortalForApp portalForApp) {
        if (portalForApp.getCurrentInst().getId() != null) {
            if (!template.exists(portalForApp.getCurrentInst().getId(), Institution.class))
                return false;
            portalForApp.getCurrentInst().setName(null);
        } else {
            if(portalForApp.getCurrentInst().getName() == null)
                return false;
        }
        for (PortalForApp.Institution institution: portalForApp.getInstitutions()) {
            if (institution.getId() != null) {
                if (!template.exists(institution.getId(), Institution.class))
                    return false;
                institution.setName(null);
            } else {
                if (institution.getName() == null)
                    return false;
            }
        }
        return true;
    }
}
