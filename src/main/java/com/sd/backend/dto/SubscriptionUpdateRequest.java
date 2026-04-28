package com.sd.backend.dto;

import com.sd.backend.model.enums.UserTier;
import com.sd.backend.model.enums.CurrencyCode;
import com.sd.backend.model.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "{validation.category.required}")
    private String category;

    private UserTier tier;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private CurrencyCode currency;

    private BillingCycle billingCycle;

    private Integer billingDay;

    private Integer billingMonth;

    private LocalDate endDate;

    private Boolean reminderEnabled;

    private Boolean isFreeTrial;

    private java.util.List<String> jointEmails;
}
