package com.sd.backend.controller;

import com.sd.backend.model.SubscriptionInvitation;
import com.sd.backend.security.UserPrincipal;
import com.sd.backend.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Joint subscription invitation management APIs")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

        private final InvitationService invitationService;

        @GetMapping("/pending")
        @Operation(summary = "Get pending invitations", description = "Get all pending joint subscription invitations for the authenticated user")
        public ResponseEntity<List<SubscriptionInvitation>> getPendingInvitations(
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                List<SubscriptionInvitation> invitations = invitationService
                                .getPendingInvitationsForUser(userPrincipal.getEmail());
                return ResponseEntity.ok(invitations);
        }

        @PostMapping("/{id}/accept")
        @Operation(summary = "Accept invitation", description = "Accept a joint subscription invitation")
        public ResponseEntity<Void> acceptInvitation(
                        @PathVariable("id") String id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                invitationService.acceptInvitation(id, userPrincipal.getId());
                return ResponseEntity.ok().build();
        }

        @PostMapping("/{id}/reject")
        @Operation(summary = "Reject invitation", description = "Reject a joint subscription invitation")
        public ResponseEntity<Void> rejectInvitation(
                        @PathVariable("id") String id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                invitationService.rejectInvitation(id, userPrincipal.getId());
                return ResponseEntity.ok().build();
        }
}
