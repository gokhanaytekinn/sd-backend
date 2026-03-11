package com.sd.backend.scheduler;

import com.sd.backend.service.NotificationBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderScheduler {

    private final NotificationBusinessService notificationBusinessService;

    @Scheduled(cron = "${app.scheduler.subscription-reminder.cron:0 30 12 * * ?}", zone = "${app.scheduler.timezone:Europe/Istanbul}")
    public void sendSubscriptionReminders() {
        log.info("Scheduler Triggered: Subscription Reminder Task");
        notificationBusinessService.processSubscriptionReminders();
    }
}
