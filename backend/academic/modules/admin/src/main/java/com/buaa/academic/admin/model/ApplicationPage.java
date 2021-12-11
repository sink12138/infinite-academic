package com.buaa.academic.admin.model;

import com.buaa.academic.document.system.Application;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationPage {

    @ApiModelProperty(value = "当前页上的所有申请项目")
    private List<Application> applications = new ArrayList<>();

    @ApiModelProperty(value = "总页数")
    private int pageCount;

    public void add(Application application) {
        this.applications.add(application);
    }

}
