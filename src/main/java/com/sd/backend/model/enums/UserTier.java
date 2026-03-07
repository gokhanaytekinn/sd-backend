package com.sd.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserTier {
    FREE(1),
    MONTHLY(2),
    YEARLY(3);

    private final int value;

    UserTier(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static UserTier fromValue(int value) {
        for (UserTier tier : values()) {
            if (tier.value == value) {
                return tier;
            }
        }
        return FREE; // Hata fırlatmak yerine güvenli varsayılan atama
    }
}
