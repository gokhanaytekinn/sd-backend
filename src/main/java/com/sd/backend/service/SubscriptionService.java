package com.sd.backend.service;

import com.sd.backend.dto.FlagSuspiciousRequest;
import com.sd.backend.dto.SubscriptionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.UnauthorizedException;
import com.sd.backend.exception.BadRequestException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptions(String userId, SubscriptionStatus status, Boolean isSuspicious) {
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
    public SubscriptionResponse getSubscription(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        
        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to subscription");
        }
        
        return toResponse(subscription);
    }
    
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setName(request.getName());
        subscription.setIcon(request.getIcon());
        subscription.setTier(request.getTier());
        subscription.setAmount(request.getAmount());
        subscription.setCurrency(request.getCurrency());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(request.getStartDate());
        subscription.setRenewalDate(calculateRenewalDate(request.getStartDate(), request.getBillingCycle()));
        subscription.setIsSuspicious(false);
        subscription.setIsApproved(false);
        
        subscription = subscriptionRepository.save(subscription);
        
        return toResponse(subscription);
    }
    
    @Transactional
    public void cancelSubscription(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        
        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to subscription");
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
    public SubscriptionResponse flagAsSuspicious(String id, String reason) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        
        subscription.setIsSuspicious(true);
        subscription.setSuspiciousReason(reason);
        subscription.setStatus(SubscriptionStatus.PENDING_APPROVAL);
        
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }
    
    @Transactional
    public SubscriptionResponse approveSubscription(String id, String approvedBy) {
        Subscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        
        subscription.setIsApproved(true);
        subscription.setApprovedAt(LocalDateTime.now());
        subscription.setApprovedBy(approvedBy);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }
    
    private LocalDate calculateRenewalDate(LocalDate startDate, String billingCycle) {
        return switch (billingCycle.toLowerCase()) {
            case "monthly" -> startDate.plusMonths(1);
            case "yearly" -> startDate.plusYears(1);
            case "quarterly" -> startDate.plusMonths(3);
            default -> startDate.plusMonths(1);
        };
    }
    
    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
            subscription.getId(),
            subscription.getUser().getId(),
            subscription.getName(),
            subscription.getIcon(),
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
