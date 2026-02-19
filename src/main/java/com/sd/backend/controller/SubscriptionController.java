package com.sd.backend.controller;

import com.sd.backend.dto.FlagSuspiciousRequest;
import com.sd.backend.dto.SubscriptionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.dto.SubscriptionUpdateRequest;
import com.sd.backend.exception.BadRequestException;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management APIs")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Get user subscriptions", description = "Get all subscriptions for the authenticated user with optional filters")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Boolean isSuspicious) {
        String userId = userDetails.getUsername();
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(userId, status, isSuspicious);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/suspicious")
    @Operation(summary = "Get suspicious subscriptions", description = "Get all subscriptions flagged as suspicious")
    public ResponseEntity<List<SubscriptionResponse>> getSuspiciousSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSuspiciousSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        SubscriptionResponse subscription = subscriptionService.getSubscription(id, userId);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        SubscriptionResponse subscription = subscriptionService.createSubscription(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subscription", description = "Update an existing subscription's details")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @PathVariable String id,
            @Valid @RequestBody SubscriptionUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request, userId);
        return ResponseEntity.ok(subscription);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        subscriptionService.cancelSubscription(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate subscription", description = "Reactivate a cancelled subscription")
    public ResponseEntity<Void> reactivateSubscription(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        subscriptionService.reactivateSubscription(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reminder")
    @Operation(summary = "Toggle reminder", description = "Toggle the reminder enabled status for a subscription")
    public ResponseEntity<SubscriptionResponse> toggleReminder(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        SubscriptionResponse subscription = subscriptionService.toggleReminder(id, userId);
        return ResponseEntity.ok(subscription);
    }

    @PatchMapping("/{id}/flag")
    public ResponseEntity<SubscriptionResponse> flagAsSuspicious(
            @PathVariable String id,
            @Valid @RequestBody FlagSuspiciousRequest request) {
        SubscriptionResponse subscription = subscriptionService.flagAsSuspicious(id, request.getReason());
        return ResponseEntity.ok(subscription);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<SubscriptionResponse> approveSubscription(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String approvedBy = userDetails.getUsername();
        SubscriptionResponse subscription = subscriptionService.approveSubscription(id, approvedBy);
        return ResponseEntity.ok(subscription);
    }
}
