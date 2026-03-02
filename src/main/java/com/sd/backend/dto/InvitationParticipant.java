package com.sd.backend.dto;

import com.sd.backend.model.SubscriptionInvitation.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationParticipant {
    private String email;
    private String name;
    private InvitationStatus status;
}
