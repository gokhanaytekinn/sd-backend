package com.sd.backend.service;

import com.sd.backend.dto.FlagSuspiciousRequest;
import com.sd.backend.dto.SubscriptionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.model.Subscription;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptions(UUID userId, SubscriptionStatus status, Boolean isSuspicious) {
        List<Subscription> subscriptions;
        
        if (status != null) {
            subscriptions = subscriptionRepository.findByUserIdAndStatus(userId, status);
        } else if (isSuspicious != null) {
            subscriptions = subscriptionRepository.findByUserIdAndIsSuspicious(userId, isSuspicious);
        } else {
            subscriptions = subscriptionRepository.findByUserId(userId);
        }
        
        return subscriptions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(UUID id, UUID userId) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        
        if (!subscription.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to subscription");
        }
        
        return toResponse(subscription);
    }
    
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request, UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setTier(request.getTier());
        subscription.setAmount(request.getAmount());
        subscription.setCurrency(request.getCurrency());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now());
        subscription.setRenewalDate(calculateRenewalDate(request.getBillingCycle()));
        subscription.setIsSuspicious(false);
        subscription.setIsApproved(false);
        
        subscription = subscriptionRepository.save(subscription);
        
        user.setTier(request.getTier());
        userRepository.save(user);
        
        return toResponse(subscription);
    }
    
    @Transactional
    public void cancelSubscription(UUID id, UUID userId) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        
        if (!subscription.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to subscription");
        }
        
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDate.now());
        subscriptionRepository.save(subscription);
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSuspiciousSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository
            .findByIsSuspiciousAndIsApproved(true, false);
        
        return subscriptions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public SubscriptionResponse flagAsSuspicious(UUID id, String reason) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        
        subscription.setIsSuspicious(true);
        subscription.setSuspiciousReason(reason);
        subscription.setStatus(SubscriptionStatus.PENDING_APPROVAL);
        
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }
    
    @Transactional
    public SubscriptionResponse approveSubscription(UUID id, String approvedBy) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        
        subscription.setIsApproved(true);
        subscription.setApprovedAt(LocalDateTime.now());
        subscription.setApprovedBy(approvedBy);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }
    
    private LocalDate calculateRenewalDate(String billingCycle) {
        LocalDate now = LocalDate.now();
        return switch (billingCycle.toLowerCase()) {
            case "monthly" -> now.plusMonths(1);
            case "yearly" -> now.plusYears(1);
            case "quarterly" -> now.plusMonths(3);
            default -> now.plusMonths(1);
        };
    }
    
    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
            subscription.getId(),
            subscription.getUser().getId(),
            subscription.getStatus(),
            subscription.getTier(),
            subscription.getStartDate(),
            subscription.getEndDate(),
            subscription.getRenewalDate(),
            subscription.getIsSuspicious(),
            subscription.getSuspiciousReason(),
            subscription.getIsApproved(),
            subscription.getApprovedAt(),
            subscription.getApprovedBy(),
            subscription.getAmount(),
            subscription.getCurrency(),
            subscription.getBillingCycle(),
            subscription.getCreatedAt(),
            subscription.getUpdatedAt()
        );
    }
}
