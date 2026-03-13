package com.sd.backend.scheduler;

import com.sd.backend.service.NotificationBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyEngagementScheduler {

    private final NotificationBusinessService notificationBusinessService;

    @Scheduled(cron = "${app.scheduler.daily-engagement.cron:0 0 0 * * ?}", zone = "${app.scheduler.timezone:UTC}")
    public void sendDailyEngagementNotifications() {
        log.info("Scheduler Triggered: Daily Engagement Task");
        notificationBusinessService.processDailyEngagementNotifications();
    }
}
