package com.sd.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BillingCycle {
    MONTHLY(1),
    YEARLY(2),
    WEEKLY(3),
    QUARTERLY(4);

    private final int value;

    BillingCycle(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static BillingCycle fromValue(int value) {
        for (BillingCycle cycle : values()) {
            if (cycle.value == value) {
                return cycle;
            }
        }
        throw new IllegalArgumentException("Invalid BillingCycle value: " + value);
    }
}
