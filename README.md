# 1. Proje Başlığı

**Sub Tracker Backend API**

# 2. Proje Açıklaması

> [!NOTE]
> Bu uygulamanın mimarisi, kod tabanı ve iş mantığı %100 oranında Yapay Zeka (AI) destekli araçlar tarafından tasarlanmış ve kodlanmıştır.

**Sub Tracker Backend**, abonelik ve düzenli harcama yönetimi uygulamasının arka uç servisidir. Kullanıcıların verilerini güvenle saklar, premium abonelik yönetimini üstlenir ve kapsamlı analitik raporların oluşturulması için REST tabanlı bir API sunar.

# 3. Özellikler

- **Kimlik Doğrulama:** BCrypt ile güvenli şifreleme ve 7 gün geçerli JWT tabanlı yetkilendirme altyapısı.
- **Güvenli Veri Yönetimi:** Kullanıcılara ve aboneliklere ait verilerin tek merkezden güvenle yönetimi.
- **Rol ve Seviye Sistemi:** Ücretsiz ve Premium kullanıcı seviyelerinin yetki kısıtlamaları.

# 4. Teknolojiler

Arka uç servisleri, modern ve yüksek performanslı araçlarla inşa edilmiştir:

- **Çekirdek:** Java 17, Spring Boot 3.2.1
- **Güvenlik:** Spring Security, JWT (JJWT 0.12.3), BCrypt
- **Veritabanı:** Spring Data MongoDB, MongoDB Cloud (Atlas)
- **API Belgelendirmesi:** Swagger/OpenAPI
- **Bağımlılık Yönetimi:** Maven, Lombok

# 5. Mimari

**Çok Katmanlı Mimari:**
Uygulama temel olarak 3 ana katmana ayrılmıştır:
- **Controller Katmanı:** REST API uç noktalarının sunulduğu ve HTTP isteklerinin karşılandığı katmandır.
- **Service Katmanı:** İş mantığının yer aldığı katmandır. Mimaride bağımlılığı en aza indirgemek için tasarlanmıştır.
- **Repository Katmanı:** MongoDB veritabanı ile iletişimi yöneten katmandır.

Bu yapı sayesinde projenin sürdürülebilirliği, bakımı ve test edilebilirliği en üst seviyeye çıkarılmıştır.

# 6. Proje Yapısı

Projenin temel dizin yapısı:

```
src/main/java/com/sd/backend/
├── controller/      # REST Controller Sınıfları (Örn: AuthController, SubscriptionController)
├── service/        # İş sınıfları (Örn: AuthService)
├── repository/     # MongoDB Repository Sınıfları
├── model/          # Veritabanı Varlık Modelleri (MongoDB Dokümanları)
├── dto/            # HTTP İstek/Yanıt aktarım nesneleri
├── security/       # JWT yapılandırmaları ve Spring Security ayarları
└── exception/      # Global ve özel hata yakalama mekanizmaları
```

# 7. Kurulum

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

# 8. Yapılandırma

Yapılandırma ayarları `src/main/resources/application.properties` veya ortam değişkenleri üzerinden yapılabilir:

- **MongoDB Bağlantısı:**
  ```properties
  spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.mongodb.net/?retryWrites=true&w=majority
  spring.data.mongodb.database=sd_backend
  ```
- **Kimlik Doğrulama:** Güvenliğiniz için `jwt.secret` ortam değişkenini en az 32 karakterlik bir anahtarla güncelleyin.
- **CORS Ayarları:** İstemcinizin portuna izin vermek için `cors.allowed.origins` anahtarını kontrol edin.

# 9. Uygulama Ekranları

Arka uç servisi olduğu için bir görsel grafik arayüz bulunmamaktadır. Ancak, `http://localhost:8080/swagger-ui.html` üzerinden ulaşılabilecek interaktif Swagger API dokümantasyonu paneli ile tüm uç noktaları test edebilir, örnek istek-cevap şemalarını görüntüleyebilirsiniz.

# 10. Gelir Modeli

Backend servisi, istemciden (Android vb.) gelen **Premium** üyelik statüsünü tanımlar ve buna göre limit koyar. İstemci tarafındaki "Uygulama İçi Satın Alma" işlemi başarılı iletildiğinde kullanıcının yetki seviyesi arka uç tarafından yükseltilir (örn. Ücretsiz -> Premium).

# 11. Arka Uç Entegrasyonu

İstemci uygulamalarının arka uç API ile haberleşme kuralları:
- İstek ve yanıtlar `application/json` formatındadır.
- `/api/auth/register` ve `/api/auth/login` uç noktaları dışındaki tüm istekler, geçerli bir JWT anahtarına ihtiyaç duyar.
- İsteklerde HTTP başlığı üzerinden anahtar gönderimi: `Authorization: Bearer <anahtar_değeri>`.

# 12. Test Durumu

Kararlı API yapısı için test kapsamı sürmektedir:
- **Birim Testleri:** Çekirdek iş mantığı fonksiyonları ve servis operasyonları JUnit ve Mockito kütüphaneleriyle test edilerek stabilite sağlanmaktadır. Sürekli entegrasyona dahil edilme aşamasındadır.

# 13. Yol Haritası

Gelecekte gerçekleştirilmesi planlanan bazı önemli eklentiler:
- Daha gelişmiş kullanıcı panosu yapılandırması.
- Redis ile önbellekleme sisteminin dahil edilerek yanıt istikrarının artırılması.
- Kapsamlı üçüncü parti ödeme altyapı (örn. Stripe) ağ kancası (webhook) entegrasyonlarının eklenmesi.

# 14. Katkı Sağlama

Projeye katkıda bulunmak, özellik eklemek veya bir hata (bug) düzeltmesi gerçekleştirmek isterseniz:
1. Projeyi kendi profiliniz üzerine çatallayın ("Fork").
2. Yeni bir dal oluşturun (`git checkout -b ozellik/YeniOzellik`).
3. Değişikliklerinizi tamamlayın (`git commit -m 'Yeni Özellik: xyz'`).
4. Geliştirmenizi deponuza gönderin (`git push origin ozellik/YeniOzellik`).
5. Ardından değişiklik birleştirme isteği (Pull Request) bırakın.

# 15. Lisans

Telif Hakkı (c) 2026 Gökhan Aytekin

Tüm hakları saklıdır.

Bu depo yalnızca görüntüleme ve eğitim amaçlı olarak herkese açık bir şekilde paylaşılmıştır.

Yazarın açık yazılı izni olmadan şunları yapamazsınız:
- Bu kodu üretim ortamında kullanmak
- Kodun önemli kısımlarını kopyalamak
- Kodu yeniden dağıtmak
- Kodu değiştirmek ve dağıtmak

Bu kodu kullanmak isterseniz, lütfen yazarla iletişime geçin.

# 16. İletişim

Hata raporlama veya iş birliği için:
- Deponun "Sorunlar (Issues)" kısmından takip kartı oluşturabilirsiniz.
- Veya organizatörlerin iletişim adresleri ile bağlantıya geçebilirsiniz.
