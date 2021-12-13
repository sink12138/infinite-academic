package com.buaa.academic.spider.service.Impl;

import com.buaa.academic.spider.util.StatusCtrl;

public abstract class ProcessThread implements Runnable {

    private StatusCtrl statusCtrl;

    private Boolean headless;
}
