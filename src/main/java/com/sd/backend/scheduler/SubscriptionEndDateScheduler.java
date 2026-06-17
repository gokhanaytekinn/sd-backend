package com.sd.backend.scheduler;

import com.sd.backend.service.SubscriptionLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEndDateScheduler {

    private final SubscriptionLifecycleService subscriptionLifecycleService;

    @Scheduled(cron = "${app.scheduler.subscription-end-date.cron:0 30 13 * * ?}", zone = "${app.scheduler.timezone:Europe/Istanbul}")
    public void moveEndedSubscriptionsToPendingApproval() {
        log.info("Scheduler Triggered: Subscription End Date Task");
        subscriptionLifecycleService.processEndDatedSubscriptions();
    }
}

