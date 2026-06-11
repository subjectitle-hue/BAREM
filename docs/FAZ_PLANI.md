# Barem — Excel %100 Parite Faz Planı

Her faz sonunda bileşen ilerlemesi. **Faz 8 tamamlandığında tüm satırlar %100.**

## Bileşen hedefleri

| Bileşen | Başlangıç | F1 | F2 | F3 | F4 | F5 | F6 | F7 | F8 |
|---------|-----------|----|----|----|----|----|----|----|-----|
| **Sorgu formu (A)** | 45% | 45% | 45% | 45% | 85% | **100%** | 100% | 100% | 100% |
| **Hesap motoru (H)** | 35% | 55% | 75% | **100%** | 100% | 100% | 100% | 100% | 100% |
| **Çıktı tablosu (A sağ)** | 15% | 15% | 15% | 15% | 15% | 15% | 70% | **100%** | 100% |
| **6 grafik (G)** | 10% | 10% | 10% | 10% | 10% | 10% | 10% | 10% | **100%** |
| **Genel parite** | ~30% | ~38% | ~46% | **~53%** | ~65% | ~70% | ~82% | ~90% | **100%** |

---

## Faz 1 — M→V BL çözümleyici (MOTOR 35%→55%) ✅

**Amaç:** Excel `A!B17 → M → H!C72,C78,C81` zinciri; TS meslek oranı shortcut kaldırılır.

| Görev | Dosya | Durum |
|-------|--------|-------|
| M pay kodları export (tazminat, yan, ek) | `export_m_h_lookup.py`, `m_h_lookup.json` | ✅ |
| `vLookupPayIndicator(kod, derece)` | `ExcelEngineRepository.kt` | ✅ |
| BlResolver M kodları (72,73,74,78,81) | `BlResolver.kt` | ✅ |
| Golden #1 YHS + #2 Hemşire | `golden_scenarios.json`, `validate_all.py`, `bl_engine.py` | ✅ |

**Kriter:** Hemşire MYO Önlisans 3/4 k27 → CO132 **81.986,53 ₺** (BL72=%90); eski TS yolu ~71k idi.

**Doğrulama:** `python scripts/validate_all.py` → PASS

---

## Faz 2 — Brüt motor tam (MOTOR 55%→75%) ✅

| Görev | Detay | Durum |
|-------|--------|-------|
| CO132 dal seçimi | 105 / 113 / 128 / 68–103 + 118–126 | ✅ |
| Satır 118–126 | İlave, aile brütte, TS ikram, çocuk | ✅ |
| Satır 75 | Bölgesel tazminat (`bolgeselKod`) | ✅ iskelet |
| Satır 82–103 | V/TS lookup iskeleti (`payCodes`) | ✅ iskelet |

**Kriter:** YHS 1/4 CO132 = **72.574,48 ₺** ±0,01 (motor); Hemşire evli CO132 = **85.361,98 ₺**

**Doğrulama:** `python scripts/validate_all.py` → PASS

---

## Faz 3 — Kesinti + net (MOTOR 75%→100%) ✅

| Görev | Detay | Durum |
|-------|--------|-------|
| CO140/141 dal mantığı | Brüt dalına göre GV matrahı | ✅ |
| Aile 124–126 | Brütte; nete ayrı ekleme kaldırıldı | ✅ |
| Prim matrahı | H!133 = CO127+68+71 + TYO tier düzeltmesi | ✅ |
| Cross-column net | Golden 5 senaryo ±0,02 ₺ | ✅ |

**Kriter:** 5 golden senaryo CO166/CO172 (motor net) ±0,02 ₺ — YHS **62.189 ₺**, Hemşire evli **73.138 ₺**

**Doğrulama:** `python scripts/validate_all.py` → PASS

---

## Faz 4 — Form cascade + etiketler (FORM 45%→85%) ✅

| Görev | Detay | Durum |
|-------|--------|-------|
| Zorunlu / Opsiyonel etiketleri | `BaremFieldLabel`, wizard alanları | ✅ |
| B2→B3→B4→derece filtre | `MemurCatalogRepository` cascade + 3 dropdown | ✅ |
| Medeni hal tam liste | `a_form_options.json` (5 seçenek) | ✅ |
| Toplu sözleşme VAR/YOK | B12 — kişisel adım | ✅ |
| Yabancı dil | B11 — opsiyonel dropdown | ✅ |

---

## Faz 5 — İl / ilçe / bölgesel (FORM 85%→100%) ✅

| Görev | Detay | Durum |
|-------|--------|-------|
| VD export | `export_vd_il_ilce.py` → `vd_il_ilce.json` (81 il, 876 ilçe) | ✅ |
| `VdLookupRepository` | B13→B14→B16 cascade | ✅ |
| H satır 75 | `bolgeselCode` (M col 14) + `bolgeselKod` (VD) | ✅ |
| Wizard il/ilçe | Kişisel adım, opsiyonel dropdown | ✅ |

**Kriter:** Diş tabibi 1. Bölge CO132 **102.074 ₺** > bölgesiz **100.946 ₺**; 6. Bölge **106.589 ₺**

**Doğrulama:** `python scripts/validate_all.py` → PASS

---

## Faz 6 — A çıktı tablosu TL (ÇIKTI 15%→70%) ✅

| Görev | Detay | Durum |
|-------|--------|-------|
| 12 aylık net tablosu | `BaremMonthlyNetTable` — Ocak–Aralık | ✅ |
| 1./2. dönem + yılsonu ort. | A satır 16–18 → `firstHalfAvg` / `secondHalfAvg` / `netAylik` | ✅ |
| Sonuç ekranı genişletme | TL bloğu + dönem özet satırları | ✅ |
| YearlyCalcEngine | 1997–2026 (`h_period_map` genişletildi, premium) | ✅ |

**Doğrulama:** `python scripts/validate_all.py` → PASS

---

## Faz 7 — FX çıktıları (ÇIKTI 70%→100%) ✅

| Görev | Detay | Durum |
|-------|--------|-------|
| Dönem FX export | `export_fx_semester.py` → `fx_semester_rates.json` (İ!94/96/107/109/133) | ✅ |
| `FxOutputCalculator` + `FxRateRepository` | A!22–54: net / dönem kuru | ✅ |
| GoldDollarScreen | USD / EUR / çeyrek altın tabloları + yıllık seri (premium) | ✅ |
| Sonuç ekranı FX özeti | Yılsonu ort. USD / EUR / çeyrek | ✅ |

**Doğrulama:** `python scripts/validate_all.py` → PASS

---

## Faz 8 — G grafikleri (GRAFİK 10%→100%) ✅

| # | Grafik | Veri | Durum |
|---|--------|------|-------|
| 1 | Türk Lirası | Yıllık ort. net + güncel yıl 12 ay | ✅ |
| 2 | Dolar | net / USD (FxOutput) | ✅ |
| 3 | Euro | net / EUR | ✅ |
| 4 | Çeyrek altın | net / çeyrek altın | ✅ |
| 5 | Enflasyon vs maaş artışı | İ!ENFLASYON + YoY net % | ✅ |
| 6 | Kesinti oranları | Net/prim/GV/DV % brüt | ✅ |

**UI:** `AnalyticsChartsScreen` — 6 sekme (TL / USD / EUR / Altın / Enflasyon / Kesinti)

**Doğrulama:** `python scripts/validate_all.py` → PASS; `assembleDebug` → OK

---

## Test disiplini

- Her faz sonu: `python scripts/validate_all.py` yeşil
- Faz 3+: 5 golden senaryo
- Faz 8: manuel Excel vs APK karşılaştırma checklist

---

## Mevcut durum

- **Faz 1–8:** ✅ tamamlandı — Excel parite planı bitti
