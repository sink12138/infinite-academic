package com.buaa.academic.analysis.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class StatusResources {
    public static final Map<String, Boolean> isRunning = new HashMap<>();
    public static final Map<String, String> runningStatus = new HashMap<>();
    public static final Map<String, Thread> currentJob = new HashMap<>();
}