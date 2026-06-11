# BAREM — v-1 Talepler Faz Planı

Kaynak: `v-1 Talepler.docx` (21 madde + sonuç kutucuk tablosu)

---

## Faz 1 — Alt menü ve giriş sayfası ✅

| # | Talep | Durum |
|---|--------|--------|
| 1 | Alt menü: **Hesap, Görüş, Veri, Geçmiş, Karşılaştırma** — Hesap sendika + premium + kalan seçenekler | ✅ |
| 2 | Giriş kutuları: **Maaş, Sözleşmeli, Kamu işçisi, Emekli, Asgari ücretli, Harcırah, Yurtdışı aylığı, Diğer maaşlar** | ✅ |

**Dosyalar:** `BaremBottomBar.kt`, `HomeTile.kt`, `HomeRoute`, `HubScreens.kt`, `strings.xml`, `BaremApp.kt`

**Kriter:** 5 sekmeli alt bar; girişte 8 çalışan tipi kutusu; Hesap ekranında sendika/premium kısayolları.

---

## Faz 2 — Hizmet sınıfı adımı ✅

| # | Talep | Durum |
|---|--------|--------|
| 3 | Hizmet sınıfı etiketinde kısaltma **tek** yazılsın (satır kısalsın, tek ekranda sığsın) | ✅ |
| 4 | Hizmet sınıfı seçilince **Devam** olmadan otomatik sonraki adım | ✅ |

**Dosyalar:** `HizmetSinifiLabels.kt`, `MemurWizardScreen.kt`, `MemurWizardViewModel.kt`

---

## Faz 3 — Kadro / maaş unsurları ✅

| # | Talep | Durum |
|---|--------|--------|
| 5 | Unvanın **üstüne yıl seçimi** | ✅ |
| 6 | **Detay** alanı yalnızca veri varsa görünsün | ✅ |
| 7 | **Derece** kademe gibi grid/chip seçim | ✅ |
| 8 | Kıdem yılında «yıl» yazısı olmasın, sadece rakam | ✅ |
| 9 | Kıdem 25’ten sonrası **25+** | ✅ |

**Dosyalar:** `MemurWizardScreen.kt`, `MemurWizardViewModel.kt`, `MemurFormState.kt`, motor yıl parametresi

---

## Faz 4 — Kişisel bilgiler formu ✅

| # | Talep | Durum |
|---|--------|--------|
| 10 | Medeni hal: **Boşanmış / Dul kaldır** | ✅ |
| 11 | Medeni hal, sendika, yabancı dil **opsiyonel**; toplu sözleşme → **«Sendika var mı» Evet/Hayır** | ✅ |
| 12 | Yabancı dil: dil listesi yok; **A / B / C düzeyi** (Excel seviye mantığı) | ✅ |
| 13 | Medeni hal, sendika, dil tazminatı **chip/grid** (derece gibi) | ✅ |
| 14 | İl/ilçe yalnızca **SHS, THS, Öğretim elemanları (ÜE)** | ✅ |

**Dosyalar:** `a_form_options.json`, `MemurWizardScreen.kt`, `ExcelMemurEngine` / BL dil seviyesi

---

## Faz 5 — Sonuç sayfası (tam yenileme) ✅

| # | Talep | Durum |
|---|--------|--------|
| 15 | Üstte brüt/kesinti/net özeti **yok**; doğrudan **3’lü kutucuklarda aylık maaşlar** | ✅ |
| 16 | En üst **TL / Dolar / Euro / Altın** seçici (varsayılan TL) | ✅ |
| 17 | Dönem ortalamaları **kutucuklarda** | ✅ |
| 18 | Hemen altında **yıllık grafik** (otomatik) | ✅ |
| 19–20 | Altta **VERİ | İSTATİSTİK | GRAFİK** kutucukları → yıllık ekranlar | ✅ |
| 21 | Sonuç sayfasında **başka içerik görünmesin** | ✅ |

**Dosyalar:** `MemurWizardScreen.kt` (SonucStep), yeni `ResultScreen.kt`, `SalaryResult` FX gösterimi

---

## Faz 6 — Paket / marka ✅

| Görev | Not |
|--------|-----|
| `tr.erdaldemir.edmr` → `tr.erdaldemir.barem` | Play güncelleme stratejisi gerekir |
| `Edmr*` → `Barem*` sınıf yeniden adlandırma | Görünen ad zaten BAREM |

---

## Doğrulama ✅

| Kontrol | Komut | Sonuç (2026-06-05) |
|---------|--------|---------------------|
| Derleme | `./gradlew assembleDebug` | PASS |
| Motor golden | `python scripts/validate_all.py` | PASS (8 senaryo) |
| JVM smoke | `./gradlew testDebugUnitTest` | PASS (`ResultCurrencySmokeTest`) |
| Cihaz smoke | `./gradlew connectedDebugAndroidTest` | Emülatör/cihaz gerekir (`MemurWizardEngineSmokeTest`, `MemurWizardUiSmokeTest`) |

Tek komut: `powershell -File scripts/smoke_all.ps1`
