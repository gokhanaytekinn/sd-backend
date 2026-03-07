package com.sd.backend.service;

import com.sd.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final FcmService fcmService;
    private final ApnsService apnsService;

    public void sendNotification(User user, String title, String body, Map<String, String> data) {
        if (user == null || !Boolean.TRUE.equals(user.getNotificationsEnabled())) {
            return;
        }

        String platform = user.getPlatform();
        if ("ios".equalsIgnoreCase(platform)) {
            if (user.getApnsToken() != null && !user.getApnsToken().isEmpty()) {
                log.info("Sending APNs notification to user: {} (Sandbox: {})", user.getEmail(), user.getIsApnsSandbox());
                apnsService.sendNotification(user.getApnsToken(), title, body, Boolean.TRUE.equals(user.getIsApnsSandbox()));
            } else {
                log.warn("User {} is iOS but has no APNs token", user.getEmail());
            }
        } else {
            // Default to FCM (Android)
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                log.info("Sending FCM notification to user: {}", user.getEmail());
                fcmService.sendNotification(user.getFcmToken(), title, body, data);
            } else {
                log.warn("User {} has no FCM token", user.getEmail());
            }
        }
    }
}
