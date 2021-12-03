package com.buaa.academic.tool.validator;

public enum CronFrequency {

    HOURLY(1), DAILY(2), WEEKLY(3), MONTHLY(4), YEARLY(5);

    private final int order;

    CronFrequency(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

}
