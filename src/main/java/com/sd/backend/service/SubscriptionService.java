package com.sd.backend.service;

import com.sd.backend.dto.InvitationParticipant;
import com.sd.backend.dto.SubscriptionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.dto.SubscriptionUpdateRequest;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.UnauthorizedException;
import com.sd.backend.model.Subscription;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionInvitationRepository;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.TransactionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.sd.backend.model.enums.BillingCycle;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final InvitationService invitationService;
    private final SubscriptionInvitationRepository invitationRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptions(String userId, SubscriptionStatus status, Boolean isSuspicious) {
        List<Subscription> subscriptions;

        if (status != null) {
            subscriptions = subscriptionRepository.findByUserIdAndStatus(userId, status);
        } else if (isSuspicious != null) {
            subscriptions = subscriptionRepository.findByUserIdAndIsSuspicious(userId, isSuspicious);
        } else {
            subscriptions = subscriptionRepository.findByUserIdOrJointUserIdsContaining(userId, userId);
        }

        return subscriptions.stream()
                .map(sub -> toResponse(sub, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        boolean isOwner = subscription.getUser().getId().equals(userId);
        boolean isJointUser = subscription.getJointUserIds() != null && subscription.getJointUserIds().contains(userId);

        if (!isOwner && !isJointUser) {
            throw new UnauthorizedException("Unauthorized access to subscription");
        }

        return toResponse(subscription, userId);
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
        subscription.setBillingDay(request.getBillingDay());
        subscription.setBillingMonth(request.getBillingMonth());

        // Calculate startDate and renewalDate based on day/month
        LocalDate nextRenewal = calculateNextRenewalDate(request.getBillingDay(), request.getBillingMonth(),
                request.getBillingCycle());
        subscription.setStartDate(LocalDate.now()); // First payment is today (creation)
        subscription.setRenewalDate(nextRenewal);

        subscription.setIsSuspicious(false);
        subscription.setIsApproved(false);
        subscription.setReminderEnabled(request.getReminderEnabled() != null ? request.getReminderEnabled() : false);

        subscription = subscriptionRepository.save(subscription);

        if (request.getJointEmails() != null && !request.getJointEmails().isEmpty()) {
            invitationService.createInvitations(subscription.getId(), userId, request.getJointEmails());
        }

        return toResponse(subscription, userId);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(String id, SubscriptionUpdateRequest request, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to subscription");
        }

        if (request.getName() != null) {
            subscription.setName(request.getName());
        }
        if (request.getIcon() != null) {
            subscription.setIcon(request.getIcon());
        }
        if (request.getTier() != null) {
            subscription.setTier(request.getTier());
        }
        if (request.getAmount() != null) {
            subscription.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            subscription.setCurrency(request.getCurrency());
        }
        if (request.getBillingCycle() != null) {
            subscription.setBillingCycle(request.getBillingCycle());
        }
        if (request.getBillingDay() != null) {
            subscription.setBillingDay(request.getBillingDay());
        }
        if (request.getBillingMonth() != null) {
            subscription.setBillingMonth(request.getBillingMonth());
        }
        if (request.getReminderEnabled() != null) {
            subscription.setReminderEnabled(request.getReminderEnabled());
        }

        // Recalculate renewal date if relevant fields changed
        if (request.getBillingCycle() != null || request.getBillingDay() != null || request.getBillingMonth() != null) {
            subscription.setRenewalDate(calculateNextRenewalDate(subscription.getBillingDay(),
                    subscription.getBillingMonth(), subscription.getBillingCycle()));
        }

        subscription = subscriptionRepository.save(subscription);

        if (request.getJointEmails() != null && !request.getJointEmails().isEmpty()) {
            invitationService.createInvitations(subscription.getId(), userId, request.getJointEmails());
        }

        return toResponse(subscription, userId);
    }

    @Transactional
    public SubscriptionResponse toggleReminder(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to subscription");
        }

        // Toggle the value (handle nulls safely)
        boolean currentStatus = subscription.getReminderEnabled() != null ? subscription.getReminderEnabled() : false;
        subscription.setReminderEnabled(!currentStatus);

        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription, userId);
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

    @Transactional
    public void deleteSubscription(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to subscription");
        }

        // Delete associated records first
        // Note: transactionRepository or reminderRepository doesn't natively have
        // deleteBySubscriptionId
        // if they don't reference it. Let's see if Transaction or Reminder reference
        // subscriptionId
        // We will just remove invitation logic for now, or anything referencing the
        // subscription

        List<com.sd.backend.model.SubscriptionInvitation> invitations = invitationRepository.findBySubscriptionId(id);
        invitationRepository.deleteAll(invitations);

        transactionRepository.deleteBySubscriptionId(id);

        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public void reactivateSubscription(String id, String userId) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to subscription");
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndDate(null);

        // Calculate next renewal date
        subscription.setRenewalDate(calculateNextRenewalDate(subscription.getBillingDay(),
                subscription.getBillingMonth(), subscription.getBillingCycle()));

        subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUpcomingSubscriptions(String userId) {
        LocalDate now = LocalDate.now();
        LocalDate limit = now.plusDays(10);

        // Also check startDate for brand new subscriptions using the same logic as the
        // scheduler
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserIdOrJointUserIdsContaining(userId, userId).stream()
                .filter(sub -> {
                    if (sub.getStatus() != SubscriptionStatus.ACTIVE)
                        return false;
                    LocalDate start = sub.getStartDate();
                    LocalDate renewal = sub.getRenewalDate();
                    return (start != null && !start.isBefore(now) && !start.isAfter(limit)) ||
                            (renewal != null && !renewal.isBefore(now) && !renewal.isAfter(limit));
                })
                .sorted((s1, s2) -> {
                    // Use renewalDate if available, otherwise fallback to startDate
                    LocalDate d1 = s1.getRenewalDate() != null ? s1.getRenewalDate() : s1.getStartDate();
                    LocalDate d2 = s2.getRenewalDate() != null ? s2.getRenewalDate() : s2.getStartDate();
                    return d1.compareTo(d2);
                })
                .collect(Collectors.toList());

        return subscriptions.stream()
                .map(sub -> toResponse(sub, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSuspiciousSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository
                .findByIsSuspiciousAndIsApproved(true, false);

        return subscriptions.stream()
                .map(sub -> toResponse(sub, null))
                .collect(Collectors.toList());
    }

    public SubscriptionResponse flagAsSuspicious(String id, String reason) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        subscription.setIsSuspicious(true);
        subscription.setSuspiciousReason(reason);
        subscription.setStatus(SubscriptionStatus.PENDING_APPROVAL);

        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription, null);
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
        return toResponse(subscription, null);
    }

    @Transactional
    public SubscriptionResponse deleteParticipant(String subscriptionId, String email, String currentUserId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Sadece abonelik sahibi katılımcı silebilir");
        }

        // Find and delete the invitation
        invitationRepository.findBySubscriptionId(subscriptionId).stream()
                .filter(inv -> inv.getInviteeEmail().equalsIgnoreCase(email.trim()))
                .findFirst()
                .ifPresent(invitation -> {
                    // If accepted, also remove from jointUserIds
                    if (invitation
                            .getStatus() == com.sd.backend.model.SubscriptionInvitation.InvitationStatus.ACCEPTED) {
                        userRepository.findByEmail(email.trim().toLowerCase())
                                .ifPresent(user -> {
                                    if (subscription.getJointUserIds() != null) {
                                        subscription.getJointUserIds().remove(user.getId());
                                    }
                                });
                    }
                    invitationRepository.delete(invitation);
                });

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return toResponse(updatedSubscription, currentUserId);
    }

    private LocalDate calculateNextRenewalDate(Integer billingDay, Integer billingMonth, BillingCycle billingCycle) {
        if (billingDay == null)
            return null;
        LocalDate now = LocalDate.now();

        if (billingCycle == BillingCycle.YEARLY && billingMonth != null) {
            LocalDate target = LocalDate.of(now.getYear(), billingMonth,
                    Math.min(billingDay, LocalDate.of(now.getYear(), billingMonth, 1).lengthOfMonth()));
            if (target.isBefore(now)) {
                target = target.plusYears(1);
            }
            return target;
        } else {
            // Default to monthly if cycle is not yearly or monthly (we use day only)
            LocalDate target = now.withDayOfMonth(Math.min(billingDay, now.lengthOfMonth()));
            if (target.isBefore(now)) {
                target = target.plusMonths(1);
                // Adjust for month length again
                target = target.withDayOfMonth(Math.min(billingDay, target.lengthOfMonth()));
            }
            return target;
        }
    }

    private SubscriptionResponse toResponse(Subscription subscription, String currentUserId) {
        boolean isOwner = currentUserId != null && subscription.getUser().getId().equals(currentUserId);

        List<InvitationParticipant> participants = invitationRepository.findBySubscriptionId(subscription.getId())
                .stream()
                .map(inv -> {
                    String name = userRepository.findByEmail(inv.getInviteeEmail())
                            .map(User::getName)
                            .orElse(inv.getInviteeEmail());
                    return new InvitationParticipant(inv.getInviteeEmail(), name, inv.getStatus());
                })
                .collect(Collectors.toList());

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
                isOwner,
                participants,
                subscription.getCreatedAt(),
                subscription.getUpdatedAt());
    }
}
