package com.sd.backend.service;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionLifecycleService {

    private static final int BATCH_SIZE = 500;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    /**
     * If a subscription has an optional endDate and that date is reached,
     * it is moved from ACTIVE to PENDING_APPROVAL.
     */
    @Transactional
    public void processEndDatedSubscriptions() {
        LocalDate today = LocalDate.now();
        log.info("Starting subscription end-date check for date={}", today);

        int page = 0;
        Page<Subscription> subscriptionPage;

        do {
            subscriptionPage = subscriptionRepository.findByStatusAndEndDateIsNotNullAndEndDateLessThanEqual(
                    SubscriptionStatus.ACTIVE,
                    today,
                    PageRequest.of(page, BATCH_SIZE)
            );

            if (subscriptionPage.isEmpty()) {
                break;
            }

            for (Subscription sub : subscriptionPage.getContent()) {
                try {
                    if (sub == null) continue;
                    User user = sub.getUser();
                    if (user == null) continue;

                    sub.setStatus(SubscriptionStatus.PENDING_APPROVAL);
                    sub.setIsApproved(false);
                    sub.setApprovedAt(null);
                    sub.setApprovedBy(null);
                    subscriptionRepository.save(sub);

                    sendEndDateNotification(sub);
                } catch (Exception e) {
                    log.error("Failed to move subscription {} to PENDING_APPROVAL", sub != null ? sub.getId() : "null", e);
                }
            }

            page++;
        } while (subscriptionPage.hasNext());

        log.info("Subscription end-date task completed.");
    }

    private void sendEndDateNotification(Subscription sub) {
        try {
            User user = sub.getUser();
            if (user == null) return;

            String lang = user.getLanguage() != null ? user.getLanguage() : "tr";
            Locale locale = Locale.forLanguageTag(lang);

            String title = messageSource.getMessage(
                    "notification.enddate.title",
                    new Object[]{sub.getName()},
                    sub.getName() + " subscription ended",
                    locale
            );

            String body = messageSource.getMessage(
                    "notification.enddate.body",
                    new Object[]{sub.getName()},
                    "Your end date for " + sub.getName() + " has been reached. Please review it.",
                    locale
            );

            Map<String, String> data = new HashMap<>();
            data.put("navigate_to", "subscriptions_list?tab=1");

            notificationService.sendNotification(user, title, body, data);
        } catch (Exception e) {
            log.error("Failed to send end-date notification for subscription {}", sub != null ? sub.getId() : "null", e);
        }
    }
}

