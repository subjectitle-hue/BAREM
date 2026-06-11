# Barem — Ürün yol haritası (parite sonrası)

## Nihai sürüm prensibi

**Uygulama akışı, motor ve ekranlar bu hâliyle kalır.** Nihai sürümde değişecek tek büyük alan:

| Değişecek | Değişmeyecek |
|-----------|----------------|
| `memur/ts_meslek.json` — çok daha fazla meslek | Sihirbaz adımları |
| İlgili export scriptleri | H hesap motoru, golden testler |
| | TL / FX / grafik mantığı |

## Tek uygulama + uygulama içi satın alma (IAP)

Mağazada **tek APK** (`tr.erdaldemir.barem`). Premium, Google Play Billing ile açılır — ayrı premium APK yok.

| Ücretsiz | Premium (IAP `barem_premium`) |
|----------|--------------------------------|
| Güncel yıl maaş hesabı + TL tablosu | 1997–2026 yıllık seri |
| TL grafik sekmesi | USD / EUR / altın / enflasyon / kesinti grafikleri |
| | Yıl karşılaştırma, FX yıllık geçmişi |

### Kod

- `BillingEntitlementRepository` — Play Billing iskeleti
- `EntitlementRepository.isPremium` — tüm kapılar buna bağlı
- **Debug APK:** Sendika paneli → «Premium aç (test)» (Play Console olmadan deneme)

### Play Console

1. Uygulama oluştur → tek paket `tr.erdaldemir.barem`
2. Tek seferlik ürün: **`barem_premium`**
3. Test hesabı ekle → gerçek satın alma akışını dene

## Oturum ve geçmiş

- `CalcSessionStore` — son hesap
- `CalcHistoryRepository` — son 30 kayıt, cihazda

## Doğrulama

- Otomatik: `python scripts/validate_all.py`
- Manuel: `docs/EXCEL_MANUAL_CHECKLIST.md`

## Derleme

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
