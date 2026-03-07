package com.sd.backend.scheduler;

import com.sd.backend.model.Subscription;
import com.sd.backend.model.enums.SubscriptionStatus;
import com.sd.backend.repository.SubscriptionRepository;
import com.sd.backend.service.NotificationService;
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
    private final NotificationService notificationService;

    @Scheduled(cron = "0 48 15 * * ?", zone = "Europe/Istanbul") // 14:30 Istanbul time

    public void sendSubscriptionReminders() {
        log.info("Starting subscription reminder check...");

        // Find subscriptions due tomorrow in user's timezone (Europe/Istanbul)
        java.time.ZoneId zoneId = java.time.ZoneId.of("Europe/Istanbul");
        LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);

        List<Subscription> subscriptions = subscriptionRepository
                .findByStatusAndReminderEnabled(SubscriptionStatus.ACTIVE, true)
                .stream()
                .filter(sub -> {
                    LocalDate nextRenewal = sub.getNextRenewalDate();
                    return nextRenewal != null &&
                            !nextRenewal.isBefore(tomorrow.minusDays(1)) &&
                            !nextRenewal.isAfter(tomorrow.plusDays(1));
                })
                .collect(java.util.stream.Collectors.toList());

        log.info("Found {} potential subscriptions for tomorrow", subscriptions.size());

        for (Subscription sub : subscriptions) {
            if (sub.getUser() != null) {
                localizeAndSend(sub);
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

        notificationService.sendNotification(sub.getUser(), title, body, data);
        log.info("Sent localized ({}) reminder to user {} for subscription {}",
                language, sub.getUser().getId(), sub.getName());
    }
}
