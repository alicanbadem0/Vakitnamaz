# 🧹 Niyet — TEMİZ KURULUM (sıfırdan)

Android Studio projesi karıştıysa bu rehberi izle. Her şeyi sıfırdan, temiz kurar.

---

## 1️⃣ Eskiyi Sil

1. **Android Studio'yu kapat**
2. Masaüstündeki **`niyet-app`** klasörünü **tamamen sil** (çöp kutusuna at)

> Endişelenme — Node.js ve Android Studio kurulu kalıyor, onları tekrar kurmayacaksın.

---

## 2️⃣ Yeni Klasör Kur

Masaüstünde yeni bir **`niyet-app`** klasörü oluştur ve şu yapıyı kur:

```
niyet-app/
├── package.json              ← indirdiğin dosya
├── capacitor.config.json     ← indirdiğin dosya (.json!  .ts DEĞİL)
└── www/
    └── index.html            ← indirdiğin uygulama
```

**⚠️ Kritik kontroller:**
- `capacitor.config.json` olacak — **`.ts` uzantılı dosya OLMAYACAK**
- `www` içindeki dosyanın adı tam **`index.html`** olacak (`index (1).html` değil!)

---

## 3️⃣ Komutlar

`niyet-app` klasörünü aç → adres çubuğuna **`cmd`** yaz + Enter.

Sırayla çalıştır:

```
npm install
```

```
npx cap add android
```
*(Soru sorarsa: `n` yaz + Enter)*

```
npx cap sync
```

```
npx cap open android
```

---

## 4️⃣ İzinleri Ekle

Android Studio açılınca:

1. Sol panel: **`app`** → **`manifests`** → **`AndroidManifest.xml`** çift tıkla
2. **Ctrl + A** (tümünü seç) → **Delete**
3. Aşağıdaki kodu **yapıştır**:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <activity
            android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|smallestScreenSize|screenLayout|uiMode"
            android:name=".MainActivity"
            android:label="@string/title_activity_main"
            android:theme="@style/AppTheme.NoActionBarLaunch"
            android:launchMode="singleTask"
            android:exported="true">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

        </activity>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"></meta-data>
        </provider>

    </application>

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

</manifest>
```

4. **Ctrl + S** kaydet

---

## 5️⃣ Logo Ekle (isteğe bağlı, sonra da yapılabilir)

`logo/` klasöründeki PNG'leri şuraya kopyala:

```
niyet-app\android\app\src\main\res\
├── mipmap-mdpi\ic_launcher.png      ← niyet-48.png
├── mipmap-hdpi\ic_launcher.png      ← niyet-72.png
├── mipmap-xhdpi\ic_launcher.png     ← niyet-96.png
├── mipmap-xxhdpi\ic_launcher.png    ← niyet-144.png
└── mipmap-xxxhdpi\ic_launcher.png   ← niyet-192.png
```

Her klasördeki `ic_launcher.png` ve `ic_launcher_round.png` dosyalarının **üzerine yaz** (aynı isimle).

---

## 6️⃣ APK Üret

Android Studio: **☰ menü** → **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**

APK burada:
```
niyet-app\android\app\build\outputs\apk\debug\app-debug.apk
```

Telefona at, kur, izinleri ver.

---

## ✅ Test Listesi

- [ ] Konum izni ver → yakın camiler geliyor mu?
- [ ] Ayarlar → 🔔 Vakit Hatırlatıcı → **"Bildirimi Test Et"** → bildirim geliyor mu?
- [ ] Ayarlar → Kıble Pusulası → çalışıyor mu?
- [ ] Kur'an → bir sure aç → iniyor mu?
- [ ] Zikir → "＋ Yeni Zikir" → ekleyebiliyor musun?
- [ ] Ayarlar → Görünüm → Koyu tema → düzgün mü?
- [ ] Alt menü içeriği kapatmıyor, değil mi?

---

Takılırsan hangi adımda olduğunu ve ekran görüntüsünü gönder. 🤲
