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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Value("${apns.bundle-id:com.nexus.sd}")
    private String bundleId;

    public void sendNotification(String deviceToken, String title, String body, java.util.Map<String, String> data) {
        if (apnsClient == null) {
            log.error("APNs client is not initialized. Check your credentials.");
            return;
        }

        try {
            java.util.Map<String, Object> aps = new java.util.HashMap<>();
            java.util.Map<String, Object> alert = new java.util.HashMap<>();
            alert.put("title", title);
            alert.put("body", body);
            aps.put("alert", alert);
            aps.put("sound", "default");

            java.util.Map<String, Object> payloadMap = new java.util.HashMap<>();
            payloadMap.put("aps", aps);
            
            if (data != null) {
                payloadMap.putAll(data);
            }

            final String payload = objectMapper.writeValueAsString(payloadMap);
            final String token = TokenUtil.sanitizeTokenString(deviceToken);

            final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, bundleId, payload);
            // APNs modern gereksinimleri için push-type başlığını ekleyelim
            // Pushy kütüphanesi bazı durumlarda bunu otomatik yapabilir ancak açıkça belirtmek daha güvenlidir.

            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    apnsClient.sendNotification(pushNotification).get();

            if (pushNotificationResponse.isAccepted()) {
                log.info("Push notification accepted by APNs gateway for topic: {}", bundleId);
            } else {
                log.error("Notification rejected by the APNs gateway for device {} with topic {}: {}. Rejection reason: {}", 
                        deviceToken, bundleId, pushNotificationResponse.getRejectionReason(), pushNotificationResponse.getRejectionReason());
                
                if ("BadDeviceToken".equals(pushNotificationResponse.getRejectionReason())) {
                    log.error("DEVICE_TOKEN_INVALID: Token is no longer valid or doesn't match the environment (Sandbox vs Production).");
                } else if ("TopicDisallowed".equals(pushNotificationResponse.getRejectionReason())) {
                    log.error("BUNDLE_ID_MISMATCH: The bundle ID '{}' does not match the provisioned certificate/key.", bundleId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send push notification via APNs to token: {}", deviceToken, e);
        }
    }
}
