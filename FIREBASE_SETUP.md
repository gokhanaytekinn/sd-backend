# Firebase Kurulum ve Entegrasyon Rehberi

Bu rehber, Push Notification (Bildirim) gönderebilmek için gerekli olan `serviceAccountKey.json` dosyasını nasıl alacağınızı ve Frontend (Android) tarafında yapmanız gerekenleri anlatır.

## 1. serviceAccountKey.json Dosyasını Alma (Backend İçin)

Backend servisinin Google Firebase sunucularıyla konuşabilmesi için bu yetki dosyasına ihtiyacı vardır.

1.  [Firebase Console](https://console.firebase.google.com/) adresine gidin ve projenizi seçin (Yoksa yeni bir proje oluşturun).
2.  Sol üstteki **Dişli Çark (Ayarlar)** simgesine tıklayın ve **Project settings** (Proje ayarları) seçeneğine gidin.
3.  Üstteki sekmelerden **Service accounts** (Hizmet hesapları) sekmesine tıklayın.
4.  Alt kısımda **Firebase Admin SDK** seçili olduğundan emin olun.
5.  **Generate new private key** (Yeni özel anahtar oluştur) butonuna tıklayın.
6.  Çıkan uyarıda tekrar **Generate key** diyerek onaylayın.
7.  Bilgisayarınıza `.json` uzantılı bir dosya inecek.
8.  Bu dosyanın adını `serviceAccountKey.json` olarak değiştirin ve bana iletin veya projenin `src/main/resources` klasörüne (veya belirlediğimiz bir güvenli konuma) koyacağız.

---

## 2. Frontend (Android) Entegrasyonu

Kullanıcının telefonuna bildirim gönderebilmek için telefonun **FCM Token** (Firebase Cloud Messaging Token) bilgisini alıp Backend'e göndermemiz gerekiyor.

### Adım 1: Firebase SDK Kurulumu
Projenizin `build.gradle` dosyalarına Firebase Messaging kütüphanesini ekleyin:

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")
}
```

### Adım 2: Token Alma ve Backend'e Gönderme
Uygulamanın ana aktivitesinde (örneğin `MainActivity.kt` veya `Login` sonrası) şu kodu çalıştırarak token'ı alıp backend'e göndermeliyiz:

```kotlin
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

fun getAndSendFCMToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w("FCM", "Fetching FCM registration token failed", task.exception)
            return@addOnCompleteListener
        }

        // 1. Token'ı al
        val token = task.result
        Log.d("FCM", "Token: $token")

        // 2. Token'ı Backend'e gönder
        sendTokenToBackend(token)
    }
}

fun sendTokenToBackend(token: String) {
    // Burası Retrofit veya kendi API servisinizi kullanarak Backend'e istek atacağınız yerdir.
    // Örnek Endpoint: POST /api/users/fcm-token
    // Body: { "token": "cihaz_token_degeri_buraya" }
    
    /* 
    apiService.updateFcmToken(UpdateTokenRequest(token)).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("API", "Token sent successfully")
        }
        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("API", "Failed to send token", t)
        }
    })
    */
}
```

### Adım 3: Manifest İzni
`AndroidManifest.xml` dosyanızda internet izni olduğundan emin olun (zaten vardır ama kontrol etmekte fayda var):

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Özet
1.  **Backend** için `serviceAccountKey.json` dosyasını indirin.
2.  **Frontend** uygulamasında FCM kütüphanesini ekleyin ve yukarıdaki kod ile token'ı alıp Backend'e gönderin.
