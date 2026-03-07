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

    @PatchMapping("/push-token")
    @Operation(summary = "Update Push Token", description = "Update the push notification token for the current user based on platform")
    public ResponseEntity<Void> updatePushToken(
            @Valid @RequestBody com.sd.backend.dto.PushTokenRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPlatform(request.getPlatform());
        if ("ios".equalsIgnoreCase(request.getPlatform())) {
            user.setApnsToken(request.getToken());
        } else {
            user.setFcmToken(request.getToken());
        }
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/notifications")
    @Operation(summary = "Update Notification Settings", description = "Update the global notification preference for the current user")
    public ResponseEntity<Void> updateNotificationSettings(
            @Valid @RequestBody com.sd.backend.dto.NotificationSettingsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setNotificationsEnabled(request.getEnabled());
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
