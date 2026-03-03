package com.sd.backend.controller;

import com.sd.backend.dto.PurchaseRequest;
import com.sd.backend.model.User;
import com.sd.backend.service.PurchaseService;
import com.sd.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final AuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<User> verifyPurchase(
            @Valid @RequestBody PurchaseRequest request,
            Principal principal) {

        String userId = authService.getUserIdFromPrincipal(principal);
        User updatedUser = purchaseService.verifyAndUpgrade(userId, request.getPurchaseToken(), request.getProductId());

        return ResponseEntity.ok(updatedUser);
    }
}
