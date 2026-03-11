package com.sd.backend.service;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.User;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationBusinessService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    private static final int BATCH_SIZE = 500;

    public void processDailyEngagementNotifications() {
        log.info("Starting paginated daily engagement notification task...");

        int page = 0;
        Page<User> userPage;

        do {
            userPage = userRepository.findAll(PageRequest.of(page, BATCH_SIZE));
            log.info("Processing daily engagement, page: {}, size: {}", page, userPage.getNumberOfElements());

            for (User user : userPage.getContent()) {
                sendDailyEngagementToUser(user);
            }
            page++;
        } while (userPage.hasNext());

        log.info("Paginated daily engagement notification task completed.");
    }

    private void sendDailyEngagementToUser(User user) {
        try {
            String lang = user.getLanguage() != null ? user.getLanguage() : "tr";
            Locale locale = Locale.forLanguageTag(lang);

            String title = messageSource.getMessage("notification.daily_engagement.title", null, "SD", locale);
            String body = messageSource.getMessage("notification.daily_engagement.body", null, "Yeni bir aboneliğiniz var mı? 👀", locale);

            Map<String, String> data = new HashMap<>();
            data.put("navigate_to", "add_subscription");

            notificationService.sendNotification(user, title, body, data);
        } catch (Exception e) {
            log.error("Failed to send daily notification to user: {}", user.getEmail(), e);
        }
    }

    public void processSubscriptionReminders() {
        log.info("Starting paginated subscription reminder check...");

        ZoneId zoneId = ZoneId.of("Europe/Istanbul");
        LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);

        int page = 0;
        Page<Subscription> subscriptionPage;

        do {
            subscriptionPage = subscriptionRepository.findByStatusAndReminderEnabled(
                    SubscriptionStatus.ACTIVE, true, PageRequest.of(page, BATCH_SIZE));
            
            log.info("Processing subscription reminders, page: {}, size: {}", page, subscriptionPage.getNumberOfElements());

            for (Subscription sub : subscriptionPage.getContent()) {
                if (sub.getUser() != null) {
                    LocalDate nextRenewal = sub.getNextRenewalDate();
                    if (nextRenewal != null && !nextRenewal.isBefore(tomorrow.minusDays(1)) && !nextRenewal.isAfter(tomorrow.plusDays(1))) {
                        processReminderForSubscription(sub);
                    }
                }
            }
            page++;
        } while (subscriptionPage.hasNext());

        log.info("Paginated subscription reminder task completed.");
    }

    private void processReminderForSubscription(Subscription sub) {
        try {
            if (Boolean.TRUE.equals(sub.getIsFreeTrial())) {
                sendLocalizedNotification(sub, true);
                
                sub.setIsFreeTrial(false);
                sub.setStatus(SubscriptionStatus.PENDING_APPROVAL);
                subscriptionRepository.save(sub);
            } else {
                sendLocalizedNotification(sub, false);
            }
        } catch (Exception e) {
            log.error("Failed to process reminder for subscription: {}", sub.getId(), e);
        }
    }

    private void sendLocalizedNotification(Subscription sub, boolean isFreeTrial) {
        String lang = sub.getUser().getLanguage() != null ? sub.getUser().getLanguage() : "tr";
        Locale locale = Locale.forLanguageTag(lang);

        String titleKey = isFreeTrial ? "notification.freetrial.title" : "notification.reminder.title";
        String bodyKey = isFreeTrial ? "notification.freetrial.body" : "notification.reminder.body";

        // Provide default generic fallback if property is extremely missing
        String defaultTitle = isFreeTrial ? sub.getName() + " Trial Ending" : sub.getName() + " Subscription";
        String defaultBody = isFreeTrial ? "Your trial for " + sub.getName() + " is ending." : "Your subscription for " + sub.getName() + " is renewing.";

        String title = messageSource.getMessage(titleKey, new Object[]{sub.getName()}, defaultTitle, locale);
        String body = messageSource.getMessage(bodyKey, new Object[]{sub.getName(), sub.getAmount(), sub.getCurrency()}, defaultBody, locale);

        Map<String, String> data = new HashMap<>();
        data.put("navigate_to", isFreeTrial ? "suspicious_subscriptions" : "upcoming_subscriptions");

        notificationService.sendNotification(sub.getUser(), title, body, data);
        log.info("Sent localized ({}) reminder to user {} for subscription {}", lang, sub.getUser().getId(), sub.getName());
    }
}
