package com.sd.backend.dto;

import com.sd.backend.model.enums.UserTier;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpdateRequest {

    private String name;

    private String icon;

    private UserTier tier;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    private String billingCycle;

    private LocalDate startDate;
}
