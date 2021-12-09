package com.buaa.academic.account.model;

import com.buaa.academic.document.system.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationPage {
    private List<Application> applications;
    private Integer pageCount;
}
