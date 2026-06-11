# Excel ↔ APK manuel kontrol listesi

Golden motor testleri (`validate_all.py`) geçtikten sonra, APK’da aşağıdaki senaryoları Excel **Mehmet Gürer.xlsx** ile karşılaştırın. Tolerans: net ±0,02 ₺.

## Hazırlık

- [ ] Excel şifresi açık, aktif yıl **2026** (H!CN/CO)
- [ ] APK: **premiumDebug** (yıllık seri için) veya free (yalnızca güncel yıl)
- [ ] Aynı form girdileri (medeni hal, kademe, kıdem, il/ilçe)

## Senaryo 1 — YHS Hizmetli 1/4

| Alan | Değer |
|------|--------|
| Unvan / derece | Hizmetli 1/4 |
| Kıdem | 25 |
| Medeni | Bekâr |

- [ ] CO132 ≈ **72.574,48 ₺**
- [ ] Ort. net ≈ **64.357 ₺** (62189 + 66525 ort.)
- [ ] Aylık net tablosu Ocak–Aralık Excel A!4–15 ile uyumlu

## Senaryo 2 — Hemşire MYO 3/4 k27 evli

- [ ] CO132 ≈ **85.361,98 ₺**
- [ ] Ort. net ≈ **75.689 ₺**

## FX (premium)

- [ ] USD Ocak: net / İ!AJ94 ≈ Excel A!22
- [ ] Çeyrek altın: net / İ!AJ133 ≈ Excel A!58

## Grafikler

- [ ] TL sekmesi — son yıl noktası = sonuç ekranı ort. net
- [ ] Enflasyon sekmesi — İ!ENFLASYON ile aynı yıl değerleri
- [ ] Geri → Sonuç ekranına dönüş (seçim korunuyor)

## Geçmiş

- [ ] Hesap sonrası **Geçmiş**’te kayıt görünür
- [ ] **Aç** → aynı kadro ile yeniden hesap, sonuç ±0,02 ₺

## Not

Yeni meslek eklendiğinde yalnızca ilgili satırın CO132/net değerlerini Excel’de aynı girdilerle spot kontrol edin; motor formülü değişmez.
