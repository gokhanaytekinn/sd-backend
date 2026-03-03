package com.sd.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CurrencyCode {
    TRY(1),
    USD(2),
    EUR(3),
    GBP(4),
    RUB(5),
    AZN(6),
    KZT(7);

    private final int value;

    CurrencyCode(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static CurrencyCode fromValue(int value) {
        for (CurrencyCode currency : values()) {
            if (currency.value == value) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Invalid CurrencyCode value: " + value);
    }
}
