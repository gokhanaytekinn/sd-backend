package com.sd.backend.service;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApnsService {

    private final ApnsClient apnsClient;

    @Value("${apns.bundle-id:com.nexus.sd}")
    private String bundleId;

    public void sendNotification(String deviceToken, String title, String body) {
        if (apnsClient == null) {
            log.error("APNs client is not initialized. Check your credentials.");
            return;
        }

        final String payload = String.format("{\"aps\":{\"alert\":{\"title\":\"%s\",\"body\":\"%s\"},\"sound\":\"default\"}}", title, body);
        final String token = TokenUtil.sanitizeTokenString(deviceToken);

        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, bundleId, payload);

        try {
            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    apnsClient.sendNotification(pushNotification).get();

            if (pushNotificationResponse.isAccepted()) {
                log.info("Push notification accepted by APNs gateway.");
            } else {
                log.error("Notification rejected by the APNs gateway: {}", pushNotificationResponse.getRejectionReason());
            }
        } catch (Exception e) {
            log.error("Failed to send push notification via APNs", e);
        }
    }
}
