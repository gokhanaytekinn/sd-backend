package com.sd.backend.repository;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
        List<Subscription> findByUserId(String userId);

        List<Subscription> findByUserIdOrJointUserIdsContaining(String userId, String jointUserId);

        List<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

        List<Subscription> findByUserIdAndIsSuspicious(String userId, Boolean isSuspicious);

        List<Subscription> findByIsSuspiciousAndIsApproved(Boolean isSuspicious, Boolean isApproved);

        List<Subscription> findByStatusAndReminderEnabled(SubscriptionStatus status, Boolean reminderEnabled);

        Page<Subscription> findByStatusAndReminderEnabled(SubscriptionStatus status, Boolean reminderEnabled, Pageable pageable);

        Page<Subscription> findByStatusAndEndDateIsNotNullAndEndDateLessThanEqual(SubscriptionStatus status, LocalDate endDate, Pageable pageable);

        void deleteByUserId(String userId);
}
