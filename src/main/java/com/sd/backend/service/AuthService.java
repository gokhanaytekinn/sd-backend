package com.sd.backend.service;

import com.sd.backend.dto.*;
import com.sd.backend.exception.BadRequestException;
import com.sd.backend.exception.UnauthorizedException;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.UserTier;
import com.sd.backend.repository.UserRepository;
import com.sd.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setTier(UserTier.FREE);
        
        user = userRepository.save(user);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getId().toString(), null, null
        );
        String token = jwtTokenProvider.generateToken(authentication);
        
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getTier(),
            user.getCreatedAt()
        );
        
        return new AuthResponse(token, userResponse);
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getId().toString(), null, null
        );
        String token = jwtTokenProvider.generateToken(authentication);
        
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getTier(),
            user.getCreatedAt()
        );
        
        return new AuthResponse(token, userResponse);
    }
}
