package com.sd.backend.model;

import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.model.enums.UserTier;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    private String id;

    @DBRef
    private User user;

    private SubscriptionStatus status;

    private String name;

    private String icon;

    private UserTier tier;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate renewalDate;

    private Boolean isSuspicious = false;

    private String suspiciousReason;

    private Boolean isApproved = false;

    private LocalDateTime approvedAt;

    private String approvedBy;

    private BigDecimal amount;

    private String currency;

    private String billingCycle;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @DBRef
    private List<Transaction> transactions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription)) return false;
        Subscription that = (Subscription) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
