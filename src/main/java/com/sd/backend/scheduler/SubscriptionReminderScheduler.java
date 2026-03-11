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

    @Scheduled(cron = "0 30 12 * * ?", zone = "Europe/Istanbul") // 14:30 Istanbul time

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
                if (Boolean.TRUE.equals(sub.getIsFreeTrial())) {
                    localizeAndSendFreeTrialEnd(sub);
                    
                    // Convert trial into pending approval state
                    sub.setIsFreeTrial(false);
                    sub.setStatus(SubscriptionStatus.PENDING_APPROVAL);
                    subscriptionRepository.save(sub);
                } else {
                    localizeAndSend(sub);
                }
            }
        }
    }

    private void localizeAndSendFreeTrialEnd(Subscription sub) {
        String language = sub.getUser().getLanguage() != null ? sub.getUser().getLanguage() : "tr";
        String title;
        String body;

        switch (language.toLowerCase()) {
            case "en":
                title = sub.getName() + " free trial ending";
                body = String.format("Your free trial for %s ends tomorrow! Cancel now if you don't want to be charged.", sub.getName());
                break;
            case "az":
                title = sub.getName() + " sınaq müddəti bitir";
                body = String.format("%s üçün sınaq müddətiniz sabah bitir! Ödəniş etməmək üçün ləğv etməyi unutmayın.", sub.getName());
                break;
            case "uz":
                title = sub.getName() + " sinov muddati tugayapti";
                body = String.format("%s uchun sinov muddatigiz ertaga tugaydi! To'lov qilinmasligi uchun bekor qiling.", sub.getName());
                break;
            case "kk":
                title = sub.getName() + " тегін сынақ мерзімі аяқталуда";
                body = String.format("%s үшін тегін сынақ мерзімі ертең аяқталады! Төлем алынбауы үшін қазір бас тартыңыз.", sub.getName());
                break;
            case "ky":
                title = sub.getName() + " акысыз сыноо мөөнөтү аяктоодо";
                body = String.format("%s үшін акысыз сыноо мөөнөтү эртең аяктайт! Акы алынбашы үшін азыр жокко чыгарыңыз.", sub.getName());
                break;
            case "tk":
                title = sub.getName() + " mugt synag möwriti gutarýar";
                body = String.format("%s üçin mugt synag möwritiňiz ertir gutarýar! Pul alynmazlygy üçin ony häzir ýatyryň.", sub.getName());
                break;
            case "tt":
                title = sub.getName() + " бушлай сынау вакыты тәмамлана";
                body = String.format("%s өчен бушлай сынау вакыты иртәгә тәмамлана! Акча алынмасын өчен хәзер баш тартыгыз.", sub.getName());
                break;
            case "ba":
                title = sub.getName() + " бушлай һынау ваҡыты тамамлана";
                body = String.format("%s өсөн бушлай һынау ваҡыты иртәгә тамамлана! Аҡса алынмаһын өсөн хәзер баш тартығыҙ.", sub.getName());
                break;
            case "ug":
                title = sub.getName() + " ھەقسىز سىناق مۇددىتى توشۇۋاتىدۇ";
                body = String.format("%s نىڭ ھەقسىز سىناق مۇددىتى ئەتە توشىدۇ! ھەق ئېلىنماسلىقى ئۈچۈн ھازırلا ۋاز كېچىڭ.", sub.getName());
                break;
            case "cv":
                title = sub.getName() + " тӳлевсĕр тĕрĕслев вăхăчĕ вĕçленет";
                body = String.format("%s тӳлевсĕр тĕрĕслев вăхăчĕ ыран вĕçленет! Укçа ан илччĕр тесен халех пăрахăçлаң.", sub.getName());
                break;
            case "sah":
                title = sub.getName() + " босхо боруобалааһын бүтэрэ чугаһаата";
                body = String.format("%s босхо боруобалааһыныҥ иртэ бүтэр! Харчы төлөммөтүн курдук билигин тохтот.", sub.getName());
                break;
            case "gag":
                title = sub.getName() + " bedava denemää vakıdı biter";
                body = String.format("%s için bedava denemää vakıdı yarın biter! Para çekilmesin deyni şindi kiyatı bozun.", sub.getName());
                break;
            case "ru":
                title = sub.getName() + " пробный период заканчивается";
                body = String.format("Пробный период для %s заканчивается завтра! Отмените сейчас, чтобы не платить.", sub.getName());
                break;
            case "de":
                title = sub.getName() + " Testversion läuft ab";
                body = String.format("Ihre Testversion für %s läuft morgen ab! Kündigen Sie jetzt, um Kosten zu vermeiden.", sub.getName());
                break;
            case "fr":
                title = sub.getName() + " fin de l'essai gratuit";
                body = String.format("Votre essai gratuit pour %s se termine demain ! Annulez maintenant pour éviter d'être débité.", sub.getName());
                break;
            case "es":
                title = sub.getName() + " final de la prueba gratuita";
                body = String.format("¡Tu prueba gratuita de %s termina mañana! Cancela ahora para evitar cargos.", sub.getName());
                break;
            case "id":
                title = sub.getName() + " uji coba gratis berakhir";
                body = String.format("Uji coba gratis Anda untuk %s berakhir besok! Batalkan sekarang agar tidak dikenakan biaya.", sub.getName());
                break;
            case "zh":
                title = sub.getName() + " 免费试用即将结束";
                body = String.format("%s 的免费试用将于明天结束！请立即取消以避免扣费。", sub.getName());
                break;
            case "tr":
            default:
                title = sub.getName() + " ücretsiz denemesi bitiyor";
                body = String.format("%s için ücretsiz deneme süreniz YARIN bitiyor! Çekim yapılmadan önce iptal etmeyi unutmayın.", sub.getName());
                break;
        }

        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("navigate_to", "suspicious_subscriptions");

        notificationService.sendNotification(sub.getUser(), title, body, data);
        log.info("Sent localized free trial ending reminder to user {} for subscription {}",
                sub.getUser().getId(), sub.getName());
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
