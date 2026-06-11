# BAREM — Windows'ta test (Mac yok)

Mac olmadan **hemen test** için Android APK kullanın. iOS için ücretsiz bulut derleme (GitHub Actions) adımları aşağıda.

## Hemen test — Android (Appetize.io)

1. Bu klasördeki APK dosyasını bulun:
   - `BAREM-0.2.1-debug.apk`
2. Tarayıcıda açın: [https://appetize.io/upload](https://appetize.io/upload)
3. APK'yı sürükleyip bırakın
4. Platform: **Android**, cihaz seçin → **Launch**
5. Size verilen link test adresinizdir, örnek:
   - `https://appetize.io/app/xxxxxxxx`

### Uygulama içi test sırası

1. **Hesap** → Maaş → memur sihirbazı → hesap yapın
2. Ana ekran → **Sendika paneli** → **Premium aç (test)**
3. **Genel İstatistik** veya sonuç → **2000–2026 Maaş İstatistiği**

---

## iOS test — GitHub Actions (Mac gerekmez, sizde Mac yok)

iOS yalnızca Apple sunucularında derlenir. Projede workflow hazır:

### Bir kez kurulum

```powershell
cd C:\Users\MEMO\AndroidStudioProjects\EDMR
git init
git add .
git commit -m "BAREM 0.2.1"
# GitHub'da boş repo oluşturun, sonra:
git remote add origin https://github.com/KULLANICI/EDMR.git
git push -u origin main
```

### Derlemeyi başlatma

1. GitHub repo → **Actions** → **iOS Simulator Build (Appetize)** → **Run workflow**
2. Bitince **Artifacts** → `Barem-ios-simulator` → zip indirin
3. [appetize.io/upload](https://appetize.io/upload) → zip'i yükleyin (iOS simulator build)

---

## Bilgisayarda Android emülatör (isteğe bağlı)

- [Android Studio](https://developer.android.com/studio) → Device Manager → emülatör
- APK'yı sürükleyip emülatöre bırakın veya:
  ```powershell
  adb install dist\BAREM-0.2.1-debug.apk
  ```
