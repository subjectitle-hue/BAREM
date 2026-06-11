# BAREM — GitHub kurulumu (subjectitle-hue)

Mac / iPhone yok → iOS testi için GitHub Actions kullanın.

**GitHub kullanıcı adınız:** `subjectitle-hue`  
**Repo adresi (oluşturduktan sonra):** `https://github.com/subjectitle-hue/BAREM`

---

## BÖLÜM A — Tarayıcıda (bir kez)

### 1. GitHub'a giriş
[https://github.com/login](https://github.com/login)

### 2. Yeni repo oluştur
[https://github.com/new](https://github.com/new)

| Alan | Ne yazın |
|------|----------|
| Repository name | `BAREM` |
| Public veya Private | İstediğiniz |
| Add README | **İşaretlemeyin** |
| Add .gitignore | **İşaretlemeyin** |
| Choose a license | None |

**Create repository** tıklayın.

### 3. Token (şifre yerine — bir kez)
1. [https://github.com/settings/tokens](https://github.com/settings/tokens)
2. **Generate new token (classic)**
3. Note: `BAREM-push`
4. Expiration: 90 days
5. **`repo`** kutusunu işaretleyin
6. **Generate token** → çıkan `ghp_...` metnini kopyalayın (bir daha gösterilmez)

---

## BÖLÜM B — PowerShell (bilgisayarınızda)

### PowerShell'i açın
Windows tuşu → `powershell` yazın → **Windows PowerShell** açın

### Komutları sırayla yapıştırın

**1. Proje klasörüne git**
```powershell
cd C:\Users\MEMO\AndroidStudioProjects\EDMR
```

**2. Dosyaları hazırla ve kaydet** (zaten yapıldıysa tekrar çalıştırabilirsiniz)
```powershell
git add .
git commit -m "BAREM 0.2.2 — Android ve iOS kaynak"
```

**3. GitHub bağlantısı**
```powershell
git branch -M main
git remote add origin https://github.com/subjectitle-hue/BAREM.git
```

`remote origin already exists` hatası alırsanız:
```powershell
git remote set-url origin https://github.com/subjectitle-hue/BAREM.git
```

**4. Yükle (push)**
```powershell
git push -u origin main
```

Sorulunca:
- **Username:** `subjectitle-hue`
- **Password:** Token'ı yapıştırın (`ghp_...`) — normal şifre değil

---

## BÖLÜM C — iOS derleme (Mac yok)

1. [https://github.com/subjectitle-hue/BAREM](https://github.com/subjectitle-hue/BAREM) açın
2. Üstte **Actions** sekmesi
3. Sol: **iOS Simulator Build (Appetize)**
4. Sağ: **Run workflow** → **Run workflow**
5. 5–10 dakika bekleyin (yeşil ✓)
6. Run'a tıklayın → en altta **Artifacts** → **Barem-0.2.2-ios-simulator** indirin

## BÖLÜM D — Appetize iOS test

1. [https://appetize.io/upload](https://appetize.io/upload)
2. İndirdiğiniz zip'i sürükleyin
3. iOS + iPhone seçin → **Launch**
4. Açılan sayfa URL'si = iOS test linkiniz

---

## Android APK (Windows — hazır)

```
C:\Users\MEMO\AndroidStudioProjects\EDMR\dist\BAREM-0.2.2-debug.apk
```

Yeniden üretmek için:
```powershell
cd C:\Users\MEMO\AndroidStudioProjects\EDMR
.\gradlew.bat packageBaremDebugApk
```
