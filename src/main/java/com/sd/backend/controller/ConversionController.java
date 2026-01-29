package com.sd.backend.controller;

import com.sd.backend.dto.ConversionRequest;
import com.sd.backend.dto.SubscriptionResponse;
import com.sd.backend.service.ConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversions")
@RequiredArgsConstructor
public class ConversionController {
    
    private final ConversionService conversionService;
    
    @PostMapping("/upgrade")
    public ResponseEntity<SubscriptionResponse> convertToPremium(
            @Valid @RequestBody ConversionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        SubscriptionResponse subscription = conversionService.convertToPremium(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }
    
    @PostMapping("/downgrade")
    public ResponseEntity<Void> downgradeToFree(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        conversionService.downgradeToFree(userId);
        return ResponseEntity.noContent().build();
    }
}
