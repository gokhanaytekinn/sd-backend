package com.sd.backend.controller;

import com.sd.backend.dto.SupportTicketRequest;
import com.sd.backend.security.UserPrincipal;
import com.sd.backend.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Tag(name = "Support", description = "Support ticket APIs")
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

    private final EmailService emailService;

    @PostMapping("/tickets")
    @Operation(summary = "Submit support ticket", description = "Send a support request to destek@nxsapps.com")
    public ResponseEntity<Void> submitSupportTicket(
            @Valid @RequestBody SupportTicketRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        emailService.sendSupportTicket(userPrincipal, request.getSubject(), request.getMessage());
        return ResponseEntity.noContent().build();
    }
}

