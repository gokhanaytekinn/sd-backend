# Backend (Sunucu) Sürüm Notları

Sunucu tarafındaki veritabanı, servis ve altyapı değişiklikleri aşağıda özetlenmiştir.

## [04.03.2026]
- **Geliştirme:** Uygulamanın sorunsuz çalışması için altyapıda yenilikler yapıldı, gereksiz hesaplamalar kaldırılarak genel sistem performansı artırıldı.

## [03.03.2026]
- **Yenilik:** Abonelik takip sistemi, tam tarih yerine "Gün ve Ay" bazlı çalışacak şekilde tamamen yenilendi. Sistem artık çok daha esnek ve hatasız sonuçlar üretiyor.
- **Hata Düzeltme:** Sistemde silinmiş veriler veya uygulamanın gönderdiği değişkenlerin (Abonelik türü, paket bilgisi vb.) sunucu tarafında yanlış yorumlanmasından kaynaklı sunucu çökmeleri ("Sunucu Hatası 500") onarıldı. Format uyuşmazlığında sistemin güvenli bir şekilde varsayılan paketlere (FREE) dönüştürülmesi sağlandı.

## [01.03.2026]
- **Hata Düzeltme:** Yaklaşan ödemeleriniz için cihazınıza gönderilmesi gereken bildirimlerin (Push Notification) zamanında ulaşmamasına sebep olan saat dilimi ve planlayıcı hataları giderildi.
- **Geliştirme:** Sunucu altyapımız, kesintileri önlemek ve daha hızlı güncellemeler sunabilmek adına optimize edilmiş konteyner (bağımsız paket) mimarisine taşındı.
- **Hata Düzeltme:** Otomatik sunucu güncellemeleri sırasında meydana gelen ve sistemin geçici olarak devredışı kalmasına neden olan dağıtım sorunları onarıldı.
