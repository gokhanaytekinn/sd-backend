package com.sd.backend.controller;

import com.sd.backend.dto.FlagSuspiciousRequest;
import com.sd.backend.dto.SubscriptionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.exception.BadRequestException;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.service.SubscriptionService;
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
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Boolean isSuspicious) {
        String userId = userDetails.getUsername();
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(userId, status, isSuspicious);
        return ResponseEntity.ok(subscriptions);
    }
    
    @GetMapping("/suspicious")
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
    
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        subscriptionService.cancelSubscription(id, userId);
        return ResponseEntity.noContent().build();
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
