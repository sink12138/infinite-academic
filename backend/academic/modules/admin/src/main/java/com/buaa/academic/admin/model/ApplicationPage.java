package com.buaa.academic.admin.model;

import com.buaa.academic.document.system.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationPage {

    private List<Application> applications = new ArrayList<>();

    private int pageCount;

    public void add(Application application) {
        this.applications.add(application);
    }

}
