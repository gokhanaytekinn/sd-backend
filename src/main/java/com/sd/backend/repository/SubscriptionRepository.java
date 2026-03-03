package com.sd.backend.repository;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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

        @Query("{ 'renewalDate' : { $gte: ?0, $lt: ?1 }, 'status' : ?2, 'reminderEnabled' : ?3 }")
        List<Subscription> findByRenewalDateRangeAndStatusAndReminderEnabled(LocalDate startDate, LocalDate endDate,
                        SubscriptionStatus status, Boolean reminderEnabled);

        @Query("{ '$or': [ { 'renewalDate' : { $gte: ?0, $lt: ?1 } }, { 'startDate' : { $gte: ?0, $lt: ?1 } } ], 'status' : ?2, 'reminderEnabled' : ?3 }")
        List<Subscription> findByStartOrRenewalDateRangeAndStatusAndReminderEnabled(LocalDate startDate,
                        LocalDate endDate,
                        SubscriptionStatus status, Boolean reminderEnabled);

        List<Subscription> findByUserIdAndRenewalDateBetweenAndStatus(String userId, LocalDate start, LocalDate end,
                        SubscriptionStatus status);

        void deleteByUserId(String userId);
}
