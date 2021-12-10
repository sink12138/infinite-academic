package com.buaa.academic.model.application;

import java.io.Serializable;
import java.util.List;

public class ModificationApp implements Serializable {
    private String name;

    private String currentInstId;

    private List<String> instIds;

    private String hIndex;

    private String gIndex;

    private List<String> interests;
}
