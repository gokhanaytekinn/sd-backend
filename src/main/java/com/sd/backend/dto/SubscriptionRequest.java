package com.sd.backend.dto;

import com.sd.backend.model.enums.UserTier;
import com.sd.backend.model.enums.CurrencyCode;
import com.sd.backend.model.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Subscription name is required")
    private String name;

    private String icon;

    @NotBlank(message = "{validation.category.required}")
    private String category;

    private UserTier tier;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private CurrencyCode currency;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Billing day is required")
    private Integer billingDay;

    private Integer billingMonth;

    private Boolean reminderEnabled = false;

    private Boolean isFreeTrial;

    private java.util.List<String> jointEmails;
}
