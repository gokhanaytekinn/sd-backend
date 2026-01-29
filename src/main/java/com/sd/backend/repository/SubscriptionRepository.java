package com.sd.backend.repository;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    List<Subscription> findByUserId(String userId);
    List<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);
    List<Subscription> findByUserIdAndIsSuspicious(String userId, Boolean isSuspicious);
    List<Subscription> findByIsSuspiciousAndIsApproved(Boolean isSuspicious, Boolean isApproved);
}
