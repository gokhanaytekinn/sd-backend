package com.sd.backend.repository;

import com.sd.backend.model.SubscriptionInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionInvitationRepository extends MongoRepository<SubscriptionInvitation, String> {
    List<SubscriptionInvitation> findByInviteeEmailAndStatus(String inviteeEmail,
            SubscriptionInvitation.InvitationStatus status);

    List<SubscriptionInvitation> findBySubscriptionId(String subscriptionId);

    void deleteByInviterId(String inviterId);

    void deleteByInviteeEmail(String inviteeEmail);
}
