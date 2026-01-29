package com.sd.backend.controller;

import com.sd.backend.dto.AuthResponse;
import com.sd.backend.dto.LoginRequest;
import com.sd.backend.dto.RegisterRequest;
import com.sd.backend.dto.UserResponse;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.model.User;
import com.sd.backend.repository.UserRepository;
import com.sd.backend.service.AuthService;
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
public class AuthController {
    
    private final AuthService authService;
    private final UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserResponse response = new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getTier(),
            user.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
}
