package com.sd.backend.repository;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserId(UUID userId);
    List<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
    List<Subscription> findByUserIdAndIsSuspicious(UUID userId, Boolean isSuspicious);
    List<Subscription> findByIsSuspiciousAndIsApproved(Boolean isSuspicious, Boolean isApproved);
}
