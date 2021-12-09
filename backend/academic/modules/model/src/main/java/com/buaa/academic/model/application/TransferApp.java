package com.buaa.academic.model.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferApp implements Serializable {
    private String title;

    private String type;

    private String patentNum;

    private String agency;

    private String agent;

    private String transferor;

    private String transferee;
}
