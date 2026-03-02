package com.sd.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "subscription_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInvitation {

    @Id
    private String id;

    private String subscriptionId;

    private String subscriptionName;

    private String inviterId;

    private String inviterName;

    private String inviteeEmail;

    private InvitationStatus status = InvitationStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
