# Backend (Spring Boot API) Changelog

Tüm veritabanı, servis veya altyapı değişiklikleri tarih sırasına göre aşağıda özetlenmiştir.

## [2026-03-03]
- **Feature/Refactor:** MongoDB `Subscription` modelinde geniş senkronizasyon yapıldı. Günü gelen aboneliklerin takibini kolaylaştırmak için tam `startDate` alanı yerine `billingDay` ve `billingMonth` mantığına geçildi. Tüm Convertor ve Servis fonksiyonları bu mantığa adapte edildi.
- **Bug Fix:** DTO revizyonları sırasında eski yapıda kalan API modüllerindeki tip uyuşmazlığı (`Type Mismatch`) ve hatalı constructor argüman dizilimleri (Controller <-> Service arasındaki) çözümlenerek API sağlıklı bir yapıya kavuşturuldu.

## [2026-03-01]
- **Bug Fix:** Yaklaşan ödemeler için kullanıcılara bildirim (Push Notification / Push Reminder) göndermekten sorumlu olan Scheduler servisi, `@EnableScheduling` eksikliği ve varsayılan TimeZone (Zaman dilimi) ayarlarındaki hatalar giderilerek stabil şekilde VPS üzerinde çalışabilir hale getirildi.
- **DevOps:** Backend projesinin sunucu üzerinde bağımsız paket halinde problemsiz koşturulabilmesi adına proje `Dockerize` edildi (Dockerfile ve yapılandırmalar oluşturuldu).
- **DevOps:** GitHub Actions üzerinden tetiklenen `deploy` iş akışlarında (workflow) başarı dönmesine rağmen uzaktaki VPS üzerindeki Docker Container'ın kendi içinde crash olarak başlatılamamasına neden olan arayüz erişim ve yol problemleri troubleshoot edilerek onarıldı.
- **DevOps:** (Genel) VPS üzerindeki docker kurulumu / ortam yolu problemleri ve servise etki eden işletimsel hata ayıklamalar yapılarak deployment kusursuz duruma getirildi.
