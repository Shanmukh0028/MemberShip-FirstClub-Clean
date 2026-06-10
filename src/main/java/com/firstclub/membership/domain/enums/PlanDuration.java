package com.firstclub.membership.domain.enums;

public enum PlanDuration {
    MONTHLY(30),
    QUARTERLY(90),
    YEARLY(365);

    private final int defaultDays;

    PlanDuration(int defaultDays) {
        this.defaultDays = defaultDays;
    }

    public int getDefaultDays() {
        return defaultDays;
    }
}
