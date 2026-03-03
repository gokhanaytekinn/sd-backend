package com.sd.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionStatus {
    COMPLETED(1),
    PENDING(2),
    FAILED(3),
    REFUNDED(4);

    private final int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static TransactionStatus fromValue(int value) {
        for (TransactionStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid TransactionStatus value: " + value);
    }
}
