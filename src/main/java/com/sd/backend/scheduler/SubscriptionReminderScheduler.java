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

    @Scheduled(cron = "0 45 19 * * ?") // 13:27 everyday
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
                    localizeAndSend(sub);
                } else {
                    log.info("Skipping reminder for user {} - global notifications disabled", sub.getUser().getId());
                }
            }
        }
    }

    private void localizeAndSend(Subscription sub) {
        String language = sub.getUser().getLanguage() != null ? sub.getUser().getLanguage() : "tr";
        String title;
        String body;

        switch (language.toLowerCase()) {
            case "en":
                title = sub.getName() + " subscription";
                body = String.format("Your %s subscription will be renewed for %s %s.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "az":
                title = sub.getName() + " abunəliyiniz";
                body = String.format("%s abunəliyiniz %s %s məbləğində yenilənəcək.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "kk":
                title = sub.getName() + " жазылымыңыз";
                body = String.format("%s жазылымыңыз %s %s сомасына жаңартылады.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "uz":
                title = sub.getName() + " obunangiz";
                body = String.format("%s obunangiz %s %s miqdorida yangilanadi.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "ky":
                title = sub.getName() + " жазылууңуз";
                body = String.format("%s жазылууңуз %s %s суммасына жаңартылат.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "tk":
                title = sub.getName() + " abunalygyňyz";
                body = String.format("%s abunalygyňyz %s %s möçberinde täzelener.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "es":
                title = "Suscripción a " + sub.getName();
                body = String.format("Su suscripción a %s se renovará por un importe de %s %s.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "ru":
                title = "Подписка на " + sub.getName();
                body = String.format("Ваша подписка на %s будет продлена на сумму %s %s.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "zh":
                title = sub.getName() + " 订阅";
                body = String.format("您的 %s 订阅将续订，金额为 %s %s。",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "fr":
                title = "Abonnement " + sub.getName();
                body = String.format("Votre abonnement %s sera renouvelé pour un montant de %s %s.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "de":
                title = sub.getName() + "-Abonnement";
                body = String.format("Ihr %s-Abonnement wird für einen Betrag von %s %s verlängert.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "id":
                title = "Langganan " + sub.getName();
                body = String.format("Langganan %s Anda akan diperpanjang sebesar %s %s.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
            case "tr":
            default:
                title = sub.getName() + " aboneliğiniz";
                body = String.format("%s aboneliğiniz %s %s tutarında yenilenecektir.",
                        sub.getName(), sub.getAmount(), sub.getCurrency());
                break;
        }

        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("navigate_to", "upcoming_subscriptions");

        fcmService.sendNotification(sub.getUser().getFcmToken(), title, body, data);
        log.info("Sent localized ({}) reminder to user {} for subscription {}",
                language, sub.getUser().getId(), sub.getName());
    }
}
