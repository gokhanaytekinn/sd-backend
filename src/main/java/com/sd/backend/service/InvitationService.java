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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final SubscriptionInvitationRepository invitationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

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
