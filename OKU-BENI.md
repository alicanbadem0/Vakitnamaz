# 📦 VakitNamaz — Hazır Paket (Tek ZIP)

Bu ZIP, projenin klasör yapısıyla **birebir aynı** düzenlendi. Tek tek dosya taşımak yerine, klasörleri **sürükleyip mevcut projenin üzerine bırakacaksın** — bilgisayar "üzerine yazılsın mı?" diye sorarsa **Evet/Değiştir** de.

---

## 1️⃣ ZIP'i Aç, İçindekileri Kopyala

ZIP'i açtığında içinde 2 ana klasör + 2 dosya göreceksin:
```
android/    ← bunu niyet-app\android\ üzerine sürükle (birleştir/üzerine yaz)
www/        ← bunu niyet-app\www\ üzerine sürükle (üzerine yaz)
package.json        ← niyet-app\ köküne kopyala (üzerine yaz)
capacitor.config.json ← niyet-app\ köküne kopyala (üzerine yaz)
```

**Windows'ta klasör birleştirme:** `android` klasörünü kopyala, `niyet-app\android` üzerine yapıştır — Windows "Bu klasörü birleştir?" sorar, **Evet** de. İçindeki dosyalar (senin zaten var olan `MainActivity.kt`, `AndroidManifest.xml` gibi) **korunur**, sadece bizim yeni dosyalarımız eklenir/güncellenir.

---

## 2️⃣ SADECE 2 Küçük Elle Ekleme Kaldı

Otomatik kopyalanamayan (senin mevcut dosyalarını bozmamak için) **2 küçük ekleme** var:

### A) `AndroidManifest.xml`'e ekle
```
niyet-app\android\app\src\main\AndroidManifest.xml
```
**`</application>`** etiketinden **hemen önce**:
```xml
    <receiver
        android:name=".VakitNamazWidget"
        android:exported="false">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/vakitnamaz_widget_info" />
    </receiver>
```

Ve **`</manifest>`** etiketinden hemen önce (izin listesinin en sonuna), eğer yoksa şu satırı da ekle:
```xml
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```
*(Bu, uygulamanın ilk açılışta otomatik pil izni isteyebilmesi için gerekli — MainActivity.kt'deki yeni kod bunu kullanıyor.)*

### B) `strings.xml`'e ekle
```
niyet-app\android\app\src\main\res\values\strings.xml
```
`</resources>` etiketinden hemen önce:
```xml
<string name="vakitnamaz_widget_desc">Sıradaki namaz vaktini ve geri sayımı gösterir</string>
```

---

## 3️⃣ Ezan Sesi (Hâlâ Senin Ekleyeceğin Tek Şey)

Ben ses/müzik üretemediğim için bu dosyayı ben veremiyorum. Kısa (5-10 sn), kullanım izni olan bir ezan klibi bul:
```
niyet-app\android\app\src\main\res\raw\ezan_kisa.mp3
```
*(Dosya adı tam bu şekilde olmalı — büyük/küçük harf, alt çizgi. Eklemesen de uygulama çökmez, sadece o ses seçeneği yerine sistem varsayılan sesini çalar.)*

---

## 4️⃣ Derle

```
npm install
```
```
npx cap sync
```

Android Studio'da: **☰ → Build → Build Bundle(s) / APK(s) → Build APK(s)**

*(Play Store sürümü için: Generate Signed Bundle)*

Telefonda **eski VakitNamaz'ı sil**, yeni APK'yı kur.

---

## ✅ Ne Değişti (Bu Paketle Gelenler)

- **Otomatik pil izni isteği** — artık uygulama ilk açıldığında kendisi soracak, Ayarlar'da aramana gerek yok
- **Widget tasarımı düzeltildi** — fazla boşluk gitti, yazılar büyüdü
- **3 bildirim sesi seçeneği** (ezan hariç 2'si hazır: `bildirim_kisa.wav`, `bildirim_uzun.wav`)
- **Ana ekran widget'ı** — günün 5 vakti + canlı geri sayım

---

## ✅ Test Listesi

- [ ] Uygulama ilk açılışta pil izni penceresi gösteriyor mu? → **İzin Ver**
- [ ] Widget'ı ekle → boşluk sorunu gitmiş mi, yazılar büyük mü?
- [ ] Birkaç saat sonra widget güncel mi (artık donmamalı)?
- [ ] Ayarlar → Bildirim → 3 sesi test et

Takıldığın yerde ekran görüntüsüyle gel. 🤲
