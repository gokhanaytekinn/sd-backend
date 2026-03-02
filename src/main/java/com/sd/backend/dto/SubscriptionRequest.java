package com.sd.backend.dto;

import com.sd.backend.model.enums.UserTier;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Subscription name is required")
    private String name;

    private String icon;

    private UserTier tier;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotBlank(message = "Billing cycle is required")
    private String billingCycle;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private Boolean reminderEnabled = false;

    private java.util.List<String> jointEmails;
}
