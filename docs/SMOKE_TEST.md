# BAREM — Smoke test

## Otomatik (CI / geliştirici)

```powershell
powershell -File scripts/smoke_all.ps1
```

| Adım | Ne doğrular |
|------|-------------|
| `validate_all.py` | Excel golden + Python motor (8 senaryo) |
| `testDebugUnitTest` | `ResultCurrencySmokeTest` — para birimi / dönem mantığı |
| `connectedDebugAndroidTest` | *(isteğe bağlı)* emülatör/cihaz bağlıyken |

### Cihaz testleri

Emülatör açık ve `adb devices` → `device` iken:

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=tr.erdaldemir.barem.MemurWizardEngineSmokeTest
```

- **Engine:** YHS Hizmetli 1/4 → 12 aylık net, golden ~62.189 / 66.525 ₺
- **UI:** Ana ekran → Maaş → YHS → Kadro adımı görünür

## Manuel (5 dk)

1. APK yükle: `app/build/outputs/apk/debug/app-debug.apk`
2. **Maaş** → **YHS** → Unvan **HİZMETLİ**, Derece **1**, Kademe **4**, Kıdem **0**
3. **Devam** × 2 → Sonuç: 12 ay kutusu, **TL / Dolar** seçici, **VERİ / İSTATİSTİK / GRAFİK** hub
4. Alt menü: Hesap, Görüş, Veri, Geçmiş, Karşılaştırma
