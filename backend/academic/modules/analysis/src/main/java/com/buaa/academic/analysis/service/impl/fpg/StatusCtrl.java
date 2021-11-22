package com.buaa.academic.analysis.service.impl.fpg;

import org.apache.hadoop.mapreduce.Job;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class
StatusCtrl {
    public static final Object STATUS_LOCK = new Object();

    public static final Map<String, Boolean> isRunning = new HashMap<>();
    public static final Map<String, String> runningStatus = new HashMap<>();
    public static final Map<String, Job> currentJob = new HashMap<>();

    public static boolean isStopped(String threadName) {
        boolean res;
        synchronized (STATUS_LOCK) {
            res = isRunning.getOrDefault(threadName, false);
        }
        return !res;
    }

    public static void stop(String jobName) throws IOException {
        Job job = currentJob.get(jobName);
        job.close();
    }
}