package com.sd.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sd.backend.dto.*;
import com.sd.backend.exception.BadRequestException;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.UnauthorizedException;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.UserTier;
import com.sd.backend.repository.ReminderRepository;
import com.sd.backend.repository.SubscriptionInvitationRepository;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.TransactionRepository;
import com.sd.backend.repository.UserRepository;
import com.sd.backend.model.Subscription;
import com.sd.backend.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final ReminderRepository reminderRepository;
    private final SubscriptionInvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, PublicKey> applePublicKeyCache = new ConcurrentHashMap<>();
    private LocalDateTime appleKeysLastFetched;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${apple.client.id:com.nexus.sd}")
    private String appleClientId;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setNotificationsEnabled(true);
        user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "tr");
        user.setTier(UserTier.FREE);

        user = userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId().toString(), null, null);
        String token = jwtTokenProvider.generateToken(authentication);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getTier(),
                user.getNotificationsEnabled(),
                user.getLanguage(),
                user.getCreatedAt());

        return new AuthResponse(token, userResponse);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId().toString(), null, null);
        String token = jwtTokenProvider.generateToken(authentication);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getTier(),
                user.getNotificationsEnabled(),
                user.getLanguage(),
                user.getCreatedAt());

        return new AuthResponse(token, userResponse);
    }

    @Transactional
    public AuthResponse googleSignIn(GoogleSignInRequest request) {
        GoogleIdToken idToken = verifyGoogleToken(request.getIdToken());

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail().toLowerCase().trim();
        String name = (String) payload.get("name");

        // Find existing user or create new one
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name : email);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setTier(UserTier.FREE);
            return userRepository.save(newUser);
        });

        log.info("Google Sign-In successful for: {}", email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId(), null, null);
        String token = jwtTokenProvider.generateToken(authentication);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getTier(),
                user.getNotificationsEnabled(),
                user.getLanguage(),
                user.getCreatedAt());

        return new AuthResponse(token, userResponse);
    }

    @Transactional
    public AuthResponse appleSignIn(AppleSignInRequest request) {
        Claims claims = verifyAppleToken(request.getIdentityToken());

        String email = claims.get("email", String.class).toLowerCase().trim();
        String name = request.getFirstName() != null ? (request.getFirstName() + " " + (request.getLastName() != null ? request.getLastName() : "")) : null;

        // Find existing user or create new one
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name.trim() : email);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setTier(UserTier.FREE);
            newUser.setLanguage("tr"); // Default language
            newUser.setNotificationsEnabled(true);
            return userRepository.save(newUser);
        });

        log.info("Apple Sign-In successful for: {}", email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId(), null, null);
        String token = jwtTokenProvider.generateToken(authentication);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getTier(),
                user.getNotificationsEnabled(),
                user.getLanguage(),
                user.getCreatedAt());

        return new AuthResponse(token, userResponse);
    }

    private Claims verifyAppleToken(String identityToken) {
        try {
            // 1. Get kid from header safely without triggering signature verification
            String[] chunks = identityToken.split("\\.");
            if (chunks.length != 3) {
                throw new BadRequestException("Invalid Apple token format");
            }

            // Manually decode the header to get kid
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            String kid = null;
            
            // Basic regex to find "kid":"..." in header JSON
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"kid\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(headerJson);
            if (matcher.find()) {
                kid = matcher.group(1);
            }

            if (kid == null) {
                throw new BadRequestException("Apple token header does not contain kid");
            }

            // 2. Get Public Key (from cache or Apple)
            PublicKey publicKey = getApplePublicKey(kid);

            // 3. Verify Signature and get Claims
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();

            // 4. Verify Audience
            String aud = claims.getAudience().iterator().next();
            if (!appleClientId.equals(aud)) {
                 if (!aud.startsWith("com.nexus.")) {
                     log.warn("Apple token audience mismatch. Expected: {}, Got: {}", appleClientId, aud);
                     // throw new UnauthorizedException("Apple token audience mismatch");
                 }
            }

            return claims;
        } catch (Exception e) {
            log.error("Apple token verification failed", e);
            throw new BadRequestException("Apple token verification failed: " + e.getMessage());
        }
    }

    private PublicKey getApplePublicKey(String kid) {
        // Refresh cache if needed (every 24 hours)
        if (appleKeysLastFetched == null || appleKeysLastFetched.isBefore(LocalDateTime.now().minusDays(1))) {
            refreshApplePublicKeys();
        }

        PublicKey key = applePublicKeyCache.get(kid);
        if (key == null) {
            // Try one more refresh if key not found
            refreshApplePublicKeys();
            key = applePublicKeyCache.get(kid);
        }

        if (key == null) {
            throw new BadRequestException("Apple public key not found for kid: " + kid);
        }
        return key;
    }

    private void refreshApplePublicKeys() {
        try {
            log.info("Fetching Apple public keys...");
            ApplePublicKeyResponse response = restTemplate.getForObject("https://appleid.apple.com/auth/keys", ApplePublicKeyResponse.class);
            if (response != null && response.getKeys() != null) {
                applePublicKeyCache.clear();
                for (ApplePublicKeyResponse.AppleJWK jwk : response.getKeys()) {
                    applePublicKeyCache.put(jwk.getKid(), constructPublicKey(jwk.getN(), jwk.getE()));
                }
                appleKeysLastFetched = LocalDateTime.now();
            }
        } catch (Exception e) {
            log.error("Failed to refresh Apple public keys", e);
        }
    }

    private PublicKey constructPublicKey(String n, String e) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(n);
        byte[] eBytes = Base64.getUrlDecoder().decode(e);
        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new UnauthorizedException("Invalid Google ID token");
            }
            return idToken;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new BadRequestException("Google token verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendResetCode(user.getEmail(), code);
    }

    @Transactional(readOnly = true)
    public void verifyCode(VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getResetCode() == null || !user.getResetCode().equals(request.getCode())) {
            throw new BadRequestException("Invalid reset code");
        }

        if (user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset code has expired");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getResetCode() == null || !user.getResetCode().equals(request.getCode())) {
            throw new BadRequestException("Invalid reset code");
        }

        if (user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset code has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Remove user from joint subscriptions
        java.util.List<Subscription> sharedSubs = subscriptionRepository.findByUserIdOrJointUserIdsContaining(null,
                userId);
        for (Subscription sub : sharedSubs) {
            if (sub.getJointUserIds() != null && sub.getJointUserIds().contains(userId)) {
                sub.getJointUserIds().remove(userId);
                subscriptionRepository.save(sub);
            }
        }

        subscriptionRepository.deleteByUserId(userId);
        transactionRepository.deleteByUserId(userId);
        reminderRepository.deleteByUserId(userId);
        invitationRepository.deleteByInviterId(userId);
        invitationRepository.deleteByInviteeEmail(user.getEmail());

        userRepository.delete(user);
    }

    public String getUserIdFromPrincipal(java.security.Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return principal.getName();
    }
}
