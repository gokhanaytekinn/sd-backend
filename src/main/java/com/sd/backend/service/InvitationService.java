package com.sd.backend.service;

import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.UnauthorizedException;
import com.sd.backend.model.Subscription;
import com.sd.backend.model.SubscriptionInvitation;
import com.sd.backend.model.User;
import com.sd.backend.repository.SubscriptionInvitationRepository;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final SubscriptionInvitationRepository invitationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    public void createInvitations(String subscriptionId, String inviterId, List<String> emails) {
        if (emails == null || emails.isEmpty())
            return;

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        for (String email : emails) {
            String normalizedEmail = email.toLowerCase().trim();
            SubscriptionInvitation invitation = new SubscriptionInvitation();
            invitation.setSubscriptionId(subscriptionId);
            invitation.setSubscriptionName(subscription.getName());
            invitation.setInviterId(inviterId);
            invitation.setInviterName(inviter.getName());
            invitation.setInviteeEmail(normalizedEmail);
            invitation.setStatus(SubscriptionInvitation.InvitationStatus.PENDING);
            invitationRepository.save(invitation);

            emailService.sendInvitationEmail(normalizedEmail, inviter.getName(), subscription.getName());

            // Send push notification if user exists and has a token
            userRepository.findByEmail(normalizedEmail).ifPresent(invitee -> {
                Locale locale = new Locale(invitee.getLanguage() != null ? invitee.getLanguage() : "tr");
                String title = messageSource.getMessage("notification.invitation.title", null, locale);
                String body = messageSource.getMessage("notification.invitation.body",
                        new Object[] { inviter.getName(), subscription.getName() }, locale);

                Map<String, String> data = new HashMap<>();
                data.put("type", "invitation");
                data.put("tab", "1");
                data.put("navigate_to", "subscriptions_list?tab=1");

                notificationService.sendNotification(invitee, title, body, data);
            });
        }
    }

    public List<SubscriptionInvitation> getPendingInvitationsForUser(String email) {
        return invitationRepository.findByInviteeEmailAndStatus(email.toLowerCase().trim(),
                SubscriptionInvitation.InvitationStatus.PENDING);
    }

    @Transactional
    public void acceptInvitation(String invitationId, String userId) {
        SubscriptionInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!invitation.getInviteeEmail().equals(user.getEmail())) {
            throw new UnauthorizedException("This invitation is not for you");
        }

        invitation.setStatus(SubscriptionInvitation.InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        Subscription subscription = subscriptionRepository.findById(invitation.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (subscription.getJointUserIds() == null) {
            subscription.setJointUserIds(new ArrayList<>());
        }

        if (!subscription.getJointUserIds().contains(userId)) {
            subscription.getJointUserIds().add(userId);
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void rejectInvitation(String invitationId, String userId) {
        SubscriptionInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!invitation.getInviteeEmail().equals(user.getEmail())) {
            throw new UnauthorizedException("This invitation is not for you");
        }

        invitation.setStatus(SubscriptionInvitation.InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }
}
