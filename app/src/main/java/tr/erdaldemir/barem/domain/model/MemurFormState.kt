package tr.erdaldemir.barem.domain.model

data class MemurFormState(
    val hizmetKolu: String? = null,
    val hizmetSinifi: String? = null,
    val meslekKod: String? = null,
    val meslekDetay: String? = null,
    /** Hesap dönemi yılı; null ise motor güncel yılı kullanır. */
    val hesapYili: Int? = null,
    val unvan: String? = null,
    val kadroDetay: String? = null,
    val derece: Int? = null,
    val kademe: Int? = null,
    val kidemYili: Int? = null,
    val medeniHal: String? = null,
    val cocukUst6: Int = 0,
    val cocukAlt6: Int = 0,
    /** A!B12 — TOPLU SÖZLEŞME İKRAMİYESİ (VAR/YOK) */
    val topluSozlesme: String = "YOK",
    /** A!B13 — il (opsiyonel) */
    val il: String? = null,
    /** A!B14 — ilçe (opsiyonel) */
    val ilce: String? = null,
    /** A!B16 — bölgesel tazminat kodu (VD lookup; il+ilçe seçilince dolar) */
    val bolgeselKod: String? = null,
    /** A!B11 — yabancı dil seviyesi A / B / C (opsiyonel) */
    val yabanciDil: String? = null,
) {
    val meslekLabel: String?
        get() = meslekKod?.let { kod ->
            if (!meslekDetay.isNullOrBlank()) "$kod — $meslekDetay" else kod
        }
}
