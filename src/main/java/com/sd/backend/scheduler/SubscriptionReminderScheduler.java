package com.sd.backend.scheduler;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.ReminderType;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.service.ReminderService;
import com.sd.backend.dto.ReminderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final ReminderService reminderService;

    // Runs daily at 12:00 PM
    @Scheduled(cron = "0 0 12 * * *")
    public void sendSubscriptionReminders() {
        log.info("Starting subscription reminder check...");

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Find subscriptions renewing tomorrow that are active and have reminders
        // enabled
        List<Subscription> subscriptions = subscriptionRepository.findByRenewalDateAndStatusAndReminderEnabled(
                tomorrow, SubscriptionStatus.ACTIVE, true);

        log.info("Found {} subscriptions renewing on {}", subscriptions.size(), tomorrow);

        for (Subscription sub : subscriptions) {
            try {
                createReminder(sub);
            } catch (Exception e) {
                log.error("Failed to create reminder for subscription {}", sub.getId(), e);
            }
        }

        log.info("Subscription reminder check completed.");
    }

    private void createReminder(Subscription subscription) {
        ReminderRequest request = new ReminderRequest();
        request.setType(ReminderType.SUBSCRIPTION_RENEWAL);
        request.setTitle("Upcoming Subscription Renewal");
        request.setMessage(String.format("Your subscription '%s' will renew tomorrow for %s %s.",
                subscription.getName(), subscription.getAmount(), subscription.getCurrency()));
        request.setScheduledAt(LocalDateTime.of(LocalDate.now(), LocalTime.now()));

        reminderService.createReminder(request, subscription.getUser().getId());
        log.info("Reminder created for subscription {}", subscription.getId());
    }
}
