package com.sd.backend.scheduler;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final FcmService fcmService;

    @Scheduled(cron = "0 29 13 * * ?") // 13:27 everyday
    public void sendSubscriptionReminders() {
        log.info("Starting subscription reminder check...");

        // Find subscriptions due tomorrow in user's timezone (Europe/Istanbul)
        java.time.ZoneId zoneId = java.time.ZoneId.of("Europe/Istanbul");
        LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);

        // Match the user example: Feb 27 21:00 UTC is Feb 28 00:00 Istanbul
        // We look for anything that falls into tomorrow (Istanbul time)
        // [tomorrow - 1, tomorrow + 1] range covers most TZ shifts
        List<Subscription> subscriptions = subscriptionRepository.findByRenewalDateRangeAndStatusAndReminderEnabled(
                tomorrow.minusDays(1), tomorrow.plusDays(1), SubscriptionStatus.ACTIVE, true);

        log.info("Found {} potential subscriptions for tomorrow", subscriptions.size());

        for (Subscription sub : subscriptions) {
            if (sub.getUser() != null && sub.getUser().getFcmToken() != null) {
                // Check user's global notification setting
                Boolean globalEnabled = sub.getUser().getNotificationsEnabled();
                if (globalEnabled == null || globalEnabled) {
                    String title = sub.getName();
                    String body = String.format("Aboneliğiniz yarın yenilenecek: %s %s",
                            sub.getAmount(), sub.getCurrency());

                    fcmService.sendNotification(sub.getUser().getFcmToken(), title, body);
                    log.info("Sent reminder to user {} for subscription {}", sub.getUser().getId(), sub.getName());
                } else {
                    log.info("Skipping reminder for user {} - global notifications disabled", sub.getUser().getId());
                }
            }
        }
    }
}
