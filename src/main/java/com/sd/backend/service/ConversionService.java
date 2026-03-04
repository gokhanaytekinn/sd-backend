package com.sd.backend.service;

import com.sd.backend.dto.ConversionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.BadRequestException;
import com.sd.backend.model.Subscription;
import com.sd.backend.model.Transaction;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.model.enums.TransactionStatus;
import com.sd.backend.model.enums.TransactionType;
import com.sd.backend.model.enums.UserTier;

import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.TransactionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversionService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public SubscriptionResponse convertToPremium(ConversionRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getTier() == UserTier.PREMIUM) {
            throw new BadRequestException("User is already premium");
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setTier(UserTier.PREMIUM);
        subscription.setAmount(request.getAmount());
        subscription.setCurrency(request.getCurrency());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBillingDay(LocalDate.now().getDayOfMonth());
        subscription.setBillingMonth(LocalDate.now().getMonthValue());
        subscription.setIsSuspicious(false);
        subscription.setIsApproved(false);

        subscription = subscriptionRepository.save(subscription);

        user.setTier(UserTier.PREMIUM);
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setSubscription(subscription);
        transaction.setType(TransactionType.UPGRADE);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription("Conversion to Premium tier");
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        return toSubscriptionResponse(subscription);
    }

    @Transactional
    public void downgradeToFree(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getTier() == UserTier.FREE) {
            throw new BadRequestException("User is already on free tier");
        }

        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);

        for (Subscription subscription : activeSubscriptions) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setEndDate(LocalDate.now());
            subscriptionRepository.save(subscription);
        }

        user.setTier(UserTier.FREE);
        userRepository.save(user);
    }

    private SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getName(),
                subscription.getIcon(),
                subscription.getStatus(),
                subscription.getTier(),
                subscription.getEndDate(),
                subscription.getBillingDay(),
                subscription.getBillingMonth(),
                subscription.getIsSuspicious(),
                subscription.getSuspiciousReason(),
                subscription.getIsApproved(),
                subscription.getApprovedAt(),
                subscription.getApprovedBy(),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getBillingCycle(),
                subscription.getReminderEnabled(),
                true, // Converter result is always for the owner
                Collections.emptyList(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt());
    }
}
