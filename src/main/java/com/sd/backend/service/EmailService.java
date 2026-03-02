package com.sd.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetCode(String to, String code) {
        log.info("Sıfırlama kodu {} için üretildi: {}", to, code);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@abonelikdedektifi.com");
            message.setTo(to);
            message.setSubject("Şifre Sıfırlama Kodu - Abonelik Dedektifi");
            message.setText("Merhaba,\n\nŞifrenizi sıfırlamak için doğrulama kodunuz: " + code +
                    "\n\nBu kod 15 dakika geçerlidir.\n\nAbonelik Dedektifi Ekibi");

            mailSender.send(message);
            log.info("E-posta başarıyla gönderildi: {}", to);
        } catch (Exception e) {
            log.error("E-posta gönderimi başarısız oldu (SMTP yapılandırmasını kontrol edin): {}", e.getMessage());
            // Geliştirme aşamasında akışın bozulmaması için hata fırlatmıyoruz.
            // Kod zaten yukarıda loglandığı için kullanıcı konsoldan bakabilir.
        }
    }

    public void sendInvitationEmail(String to, String inviterName, String subscriptionName) {
        log.info("Davet e-postası {} için gönderiliyor. Davet eden: {}, Abonelik: {}", to, inviterName,
                subscriptionName);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@abonelikdedektifi.com");
            message.setTo(to);
            message.setSubject("Ortak Abonelik Daveti - Abonelik Dedektifi");
            message.setText("Merhaba,\n\n" + inviterName + " sizi '" + subscriptionName
                    + "' isimli aboneliğe ortak olarak eklemek istiyor.\n\n" +
                    "Aboneliği kabul etmek için uygulamadaki 'Şüpheli Ödemeler' sekmesine göz atabilirsiniz.\n\n" +
                    "Abonelik Dedektifi Ekibi");

            mailSender.send(message);
            log.info("Davet e-postası başarıyla gönderildi: {}", to);
        } catch (Exception e) {
            log.error("Davet e-postası gönderimi başarısız oldu: {}", e.getMessage());
        }
    }
}
