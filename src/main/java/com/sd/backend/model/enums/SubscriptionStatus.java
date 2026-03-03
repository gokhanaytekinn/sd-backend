package com.sd.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SubscriptionStatus {
    ACTIVE(1),
    SUSPENDED(2),
    CANCELLED(3),
    PENDING_APPROVAL(4);

    private final int value;

    SubscriptionStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static SubscriptionStatus fromValue(int value) {
        for (SubscriptionStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid SubscriptionStatus value: " + value);
    }
}
