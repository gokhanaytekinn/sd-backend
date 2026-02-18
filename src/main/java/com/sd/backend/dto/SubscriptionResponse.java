package com.sd.backend.dto;

import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.model.enums.UserTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String id;
    private String userId;
    private String name;
    private String icon;
    private SubscriptionStatus status;
    private UserTier tier;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate renewalDate;
    private Boolean isSuspicious;
    private String suspiciousReason;
    private Boolean isApproved;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private BigDecimal amount;
    private String currency;
    private String billingCycle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
