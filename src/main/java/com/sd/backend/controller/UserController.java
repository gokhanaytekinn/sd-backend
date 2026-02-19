package com.sd.backend.controller;

import com.sd.backend.dto.FcmTokenRequest;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.model.User;
import com.sd.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
public class UserController {

    private final UserRepository userRepository;

    @PatchMapping("/fcm-token")
    @Operation(summary = "Update FCM Token", description = "Update the Firebase Cloud Messaging token for the current user")
    public ResponseEntity<Void> updateFcmToken(
            @Valid @RequestBody FcmTokenRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFcmToken(request.getToken());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
