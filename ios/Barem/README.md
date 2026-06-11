# BAREM iOS

Android `tr.erdaldemir.barem` uygulamasının iOS karşılığı — **sürüm 0.2.2** (versionCode 5).

Android ile aynı: `0.2.2` / build `5` / bundle `tr.erdaldemir.barem`.

## Mac'te açma

1. `ios/Barem/Barem.xcodeproj` → Xcode 15+
2. Run (⌘R)

## Mac yok — GitHub Actions

Kök dizindeki **[GITHUB-SETUP.md](../../GITHUB-SETUP.md)** dosyasını izleyin.

Özet: GitHub'a push → Actions → iOS build → artifact zip → Appetize.io

## Android ile senkron

```powershell
Copy-Item "..\..\app\src\main\assets\engine\*" "Barem\Resources\engine\" -Force
Copy-Item "..\..\app\src\main\assets\memur\*" "Barem\Resources\memur\" -Force
```

GitHub workflow bu kopyayı derleme öncesi otomatik yapar.
