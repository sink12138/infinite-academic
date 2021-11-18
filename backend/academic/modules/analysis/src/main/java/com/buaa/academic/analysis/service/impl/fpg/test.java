package com.buaa.academic.analysis.service.impl.fpg;

public class test {
    public static void main(String[] args) {
        FPGMainClass fpgMainClass = new FPGMainClass("./modules/analysis/src/main/resources/data/data.txt", 0.4, 0.6, false);
        fpgMainClass.run();
    }
}
