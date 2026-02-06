# 🎬 CineMap - Android Movie Discovery & Social App

**CineMap**, film keşfetmeyi, kişisel listeler oluşturmayı ve sosyal etkileşimi tek bir platformda birleştiren, **Kotlin** ile geliştirilmiş kapsamlı bir Android uygulamasıdır.

Bu proje, **Hibrit Veri Mimarisi** (Cloud + Local) kullanarak hem çevrimiçi hem de çevrimdışı senaryolarda kesintisiz bir kullanıcı deneyimi sunar.

---

##  Proje Hakkında

CineMap, kullanıcıların "Ne izlesem?" sorusuna yanıt verirken, aynı zamanda sinema deneyimini dijitalleştirir. Kullanıcılar filmler hakkında detaylı bilgi alabilir, yorum yapıp oylayabilir ve fiziksel dünyada en yakın sinema salonlarını harita üzerinde görüntüleyebilir.

##  Öne Çıkan Özellikler

### 1. Hibrit Veri Tabanı Mimarisi 
* **Firebase Firestore:** Tüm filmler, yorumlar ve puanlar bulutta tutulur ve anlık senkronize olur.
* **Room Database:** Kullanıcının *Favoriler*, *İzleme Listesi* ve *Geçmiş* verileri cihaz hafızasında (yerel SQL) saklanır. İnternet olmasa bile listelere erişim sağlanır.

### 2. Sosyal Etkileşim ve Oylama Sistemi 
* **Reddit Benzeri Oylama:** Kullanıcılar yorumları "Upvote" (Yukarı Oy) veya "Downvote" (Aşağı Oy) edebilir.
* **Dinamik Puanlama:** Bir filme hiç oy verilmediyse **IMDb puanı**, kullanıcılar oy verdiyse **CineMap topluluk puanı** gösterilir.

### 3. Konum Bazlı Servisler (Google Maps) 
* **Google Maps SDK:** Uygulama içinde entegre harita kullanımı.
* **Fused Location Provider:** Kullanıcının anlık konumu hassas bir şekilde alınır ve şehirdeki sinema salonları özel işaretçilerle (Marker) gösterilir.

### 4. Kullanıcı Güvenliği ve Yönetimi 
* **Firebase Authentication:** Güvenli giriş/çıkış, kayıt olma ve şifre sıfırlama işlemleri.
* **Profil Yönetimi:** Profil fotoğrafı yükleme ve kişisel ayarlar.

---

## Kullanılan Teknolojiler

* **Dil:** Kotlin
* **Mimari:** MVVM & Repository Pattern
* **UI:** XML Layouts & ViewBinding
* **Backend:** Firebase (Auth, Firestore, Storage)
* **Local DB:** Room Database
* **Harita:** Google Maps SDK & Location Services
* **Görsel Yükleme:** Glide
* **Animasyon:** Lottie Files
* **Asenkron İşlemler:** Kotlin Coroutines

---

## ⚙️ Kurulum Notları
1. Proje `google-services.json` dosyası içermemektedir. Kendi Firebase projenizi oluşturup dosyayı eklemelisiniz.
2. `AndroidManifest.xml` dosyasında Google Maps API anahtarı gereklidir.





Bu proje **Ege Negir** tarafından geliştirilmiştir.
