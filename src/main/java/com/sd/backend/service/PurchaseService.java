package com.sd.backend.service;

import com.sd.backend.model.User;
import com.sd.backend.model.enums.UserTier;
import com.sd.backend.repository.UserRepository;
import com.sd.backend.exception.ResourceNotFoundException;
import com.sd.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final UserRepository userRepository;

    @Transactional
    public User verifyAndUpgrade(String userId, String purchaseToken, String productId) {
        // In a real application, you would use the Google Play Developer API here:
        // 1. Initialize AndroidPublisher client
        // 2. Call publishers.purchases().subscriptions().get(packageName, productId,
        // purchaseToken)
        // 3. Verify the expiryTimeMillis and paymentState

        // For now, we will simulate successful verification
        if (purchaseToken == null || purchaseToken.isEmpty()) {
            throw new BadRequestException("Geçersiz satın alma token'ı");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Upgrade user to Premium
        user.setTier(UserTier.PREMIUM);
        return userRepository.save(user);
    }
}
