package com.sd.backend.scheduler;

import com.sd.backend.model.User;
import com.sd.backend.repository.UserRepository;
import com.sd.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyEngagementScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    @Scheduled(cron = "0 0 20 * * ?", zone = "Europe/Istanbul") // Every day at 20:00 Istanbul time
    public void sendDailyEngagementNotifications() {
        log.info("Starting daily engagement notification task...");

        List<User> users = userRepository.findAll();
        log.info("Sending notifications to {} users", users.size());

        for (User user : users) {
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
        log.info("Daily engagement notification task completed.");
    }
}
