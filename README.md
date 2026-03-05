# 1. Proje Başlığı

**Sub Tracker Backend API**

# 2. Proje Açıklaması (Overview)

> [!NOTE]
> Bu uygulamanın mimarisi, kod tabanı ve iş mantığı %100 oranında Yapay Zeka (AI) destekli araçlar tarafından tasarlanmış ve kodlanmıştır.

**Sub Tracker Backend**, abonelik ve düzenli harcama yönetimi uygulamasının arka uç servisidir. Kullanıcıların verilerini güvenle saklar, premium abonelik yönetimini üstlenir ve kapsamlı analitik raporların oluşturulması için REST tabanlı bir API sunar.

# 3. Özellikler (Features)

- **Kimlik Doğrulama:** BCrypt ile güvenli şifreleme ve 7 gün geçerli JWT tabanlı yetkilendirme altyapısı.
- **Güvenli Veri Yönetimi:** Kullanıcılara ve aboneliklere ait verilerin tek merkezden güvenle yönetimi.
- **Rol ve Seviye Sistemi:** Ücretsiz (Free) ve Premium kullanıcı seviyelerinin (Tiers) yetki kısıtlamaları.

# 4. Teknolojiler (Tech Stack)

Arka uç servisleri, modern ve yüksek performanslı araçlarla inşa edilmiştir:

- **Çekirdek:** Java 17, Spring Boot 3.2.1
- **Güvenlik:** Spring Security, JWT (JJWT 0.12.3), BCrypt
- **Veritabanı:** Spring Data MongoDB, MongoDB Cloud (Atlas)
- **API Belgelendirmesi:** Swagger/OpenAPI
- **Bağımlılık Yönetimi:** Maven, Lombok

# 5. Mimari (Architecture)

**Çok Katmanlı Mimari (N-Layered Architecture):**
Uygulama temel olarak 3 ana katmana ayrılmıştır:
- **Controller Katmanı:** REST API uç noktalarının (endpoints) sunulduğu ve HTTP isteklerinin karşılandığı katmandır.
- **Service Katmanı:** İş mantığının (Business Logic) yer aldığı katmandır. Mimaride bağımlılığı en aza indirgemek için tasarlanmıştır.
- **Repository Katmanı:** MongoDB veritabanı ile iletişimi yöneten katmandır.

Bu yapı sayesinde projenin sürdürülebilirliği, bakımı ve test edilebilirliği en üst seviyeye çıkarılmıştır.

# 6. Proje Yapısı (Project Structure)

Projenin temel dizin yapısı:

```
src/main/java/com/sd/backend/
├── controller/      # REST Controllers (Örn: AuthController, SubscriptionController)
├── service/        # İş sınıfları (Örn: AuthService, AnalyticsService)
├── repository/     # MongoDB Repositories
├── model/          # Veritabanı Entity modelleri (MongoDB Documents)
├── dto/            # HTTP İstek/Yanıt aktarım nesneleri
├── security/       # JWT yapılandırmaları ve Spring Security ayarları
└── exception/      # Global ve özel hata yakalama mekanizmaları
```

# 7. Kurulum (Installation / Setup)

Lokal ortamda API hizmetini başlatmak için:

1. Bağımlılıkları yükleyin ve derleyin:
   ```bash
   mvn clean package
   ```
2. Uygulamayı çalıştırın:
   ```bash
   java -jar target/sd-backend-1.0.0.jar
   # veya
   mvn spring-boot:run
   ```
3. API, varsayılan olarak `http://localhost:8080` adresinde çalışacaktır.
4. Swagger arayüzü için: `http://localhost:8080/swagger-ui.html`

# 8. Konfigürasyon (Configuration)

Konfigürasyon ayarları `src/main/resources/application.properties` veya ortam değişkenleri üzerinden yapılabilir:

- **MongoDB Bağlantısı:**
  ```properties
  spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.mongodb.net/?retryWrites=true&w=majority
  spring.data.mongodb.database=sd_backend
  ```
- **Kimlik Doğrulama:** Güvenliğiniz için `jwt.secret` ortam değişkenini en az 32 karakterlik bir anahtarla güncelleyin.
- **CORS Ayarları:** İstemcinizin portuna izin vermek için `cors.allowed.origins` anahtarını kontrol edin.

# 9. Uygulama Ekranları (Screenshots / UI)

Arka uç servisi olduğu için bir görsel grafik arayüz bulunmamaktadır. Ancak, `http://localhost:8080/swagger-ui.html` üzerinden ulaşılabilecek interaktif Swagger API dokümantasyonu paneli ile tüm uç noktaları test edebilir, örnek istek-cevap şemalarını görüntüleyebilirsiniz.

# 10. Monetization

Backend servisi, istemciden (Android vb.) gelen **Premium** üyelik statüsünü tanımlar ve buna göre limit koyar. İstemci tarafındaki "Uygulama İçi Satın Alma" (In-App Purchase) işlemi başarılı iletildiğinde kullanıcının yetki (Tier) seviyesi Backend tarafından yükseltilir (örn. Free -> Premium).

# 11. Backend Entegrasyonu

İstemci uygulamalarının arka uç API ile haberleşme kuralları:
- İstek ve yanıtlar `application/json` formatındadır.
- `/api/auth/register` ve `/api/auth/login` uç noktaları dışındaki tüm istekler, valid bir JWT token'a ihtiyaç duyar.
- İsteklerde HTTP Header üzerinden token gönderimi: `Authorization: Bearer <token_değeri>`.

# 12. Test Durumu (Testing)

Kararlı API yapısı için test kapsamı sürmektedir:
- **Birim Testleri (Unit Testing):** Çekirdek iş (Business) logic fonksiyonları ve servis operasyonları JUnit ve Mockito kütüphaneleriyle test edilerek stabilite sağlanmaktadır. Sürekli entegrasyona dahil edilme aşamasındadır.

# 13. Yol Haritası (Roadmap)

Gelecekte gerçekleştirilmesi planlanan bazı önemli eklentiler:
- Daha gelişmiş kullanıcı panosu (Dashboard) yapılandırması.
- Redis ile önbellekleme (Caching) sisteminin dahil edilerek yanıt istikrarının artırılması.
- Kapsamlı üçüncü parti ödeme altyapı (örn. Stripe) webhook entegrasyonlarının eklenmesi.

# 14. Katkı (Contributing)

Projeye katkıda bulunmak, özellik eklemek veya bir hata (bug) düzeltmesi gerçekleştirmek isterseniz:
1. Projeyi kendi profiliniz üzerine "Fork" yapın.
2. Yeni bir "Branch" oluşturun (`git checkout -b feature/YeniÖzellik`).
3. Değişikliklerinizi tamamlayın (`git commit -m 'Yeni Özellik: xyz'`).
4. Geliştirmenizi deponuza gönderin (`git push origin feature/YeniÖzellik`).
5. Ardından geri bildirim (Pull Request) bırakın.

# 15. Lisans (License)

Telif Hakkı (c) 2026 Gökhan Aytekin

Tüm hakları saklıdır.

Bu depo yalnızca görüntüleme ve eğitim amaçlı olarak herkese açık bir şekilde paylaşılmıştır.

Yazarın açık yazılı izni olmadan şunları yapamazsınız:
- Bu kodu üretim (production) ortamında kullanmak
- Kodun önemli kısımlarını kopyalamak
- Kodu yeniden dağıtmak
- Kodu değiştirmek ve dağıtmak

Bu kodu kullanmak isterseniz, lütfen yazarla iletişime geçin.

# 16. İletişim (Contact)

Hata raporlama veya iş birliği için:
- Reponun "Issues" kısmından takip kartı oluşturabilirsiniz.
- Veya organizatörlerin iletişim adresleri ile bağlantıya geçebilirsiniz.
