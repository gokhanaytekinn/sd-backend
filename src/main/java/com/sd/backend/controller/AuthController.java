package com.sd.backend.controller;

import com.sd.backend.dto.*;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.model.User;
import com.sd.backend.repository.UserRepository;
import com.sd.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

        private final AuthService authService;
        private final UserRepository userRepository;

        @PostMapping("/register")
        @Operation(summary = "Register new user", description = "Create a new user account with email and password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User successfully registered"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
        })
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
                AuthResponse response = authService.register(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping("/login")
        @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
                AuthResponse response = authService.login(request);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/google")
        @Operation(summary = "Google Sign-In", description = "Verify a Google ID token and return an app JWT. Creates a new user account if the email is not registered yet.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully authenticated via Google"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired Google ID token"),
                        @ApiResponse(responseCode = "401", description = "Google token verification failed")
        })
        public ResponseEntity<AuthResponse> googleSignIn(@Valid @RequestBody GoogleSignInRequest request) {
                AuthResponse response = authService.googleSignIn(request);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/forgot-password")
        @Operation(summary = "Forgot password", description = "Send a 6-digit reset code to user's email")
        public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
                authService.forgotPassword(request);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/verify-code")
        @Operation(summary = "Verify reset code", description = "Verify the 6-digit code sent to user's email")
        public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
                authService.verifyCode(request);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset password", description = "Reset user's password using the verification code")
        public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
                authService.resetPassword(request);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/me")
        @Operation(summary = "Get current user", description = "Get currently authenticated user information", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
                String userId = userDetails.getUsername();
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                return ResponseEntity.ok(new UserResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getName(),
                                user.getTier(),
                                user.getNotificationsEnabled(),
                                user.getLanguage(),
                                user.getCreatedAt()));
        }

        @DeleteMapping("/me")
        @Operation(summary = "Delete current user", description = "Permanently deletes the currently authenticated user and all associated data", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User successfully deleted"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
                String userId = userDetails.getUsername();
                authService.deleteAccount(userId);
                return ResponseEntity.ok().build();
        }
}
