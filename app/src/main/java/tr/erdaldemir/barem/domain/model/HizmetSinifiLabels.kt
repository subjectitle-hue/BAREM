package tr.erdaldemir.barem.domain.model

/**
 * v1-Uygulama.xlsx MEMUR sayfasındaki hizmet sınıfı etiketleri.
 */
object HizmetSinifiLabels {
    private val names = mapOf(
        "GİH" to "Genel İdare Hizmetleri",
        "AHS" to "Avukatlık Hizmetleri",
        "SHS" to "Sağlık Hizmetleri",
        "THS" to "Teknik Hizmetler",
        "DHS" to "Din Hizmetleri",
        "EHS" to "Emniyet Hizmetleri",
        "EÖHS" to "Eğitim Öğretim Hizmetleri",
        "EÖS" to "Eğitim Öğretim Hizmetleri",
        "MİAH" to "Mülki İdare Hizmetleri",
        "YHS" to "Yardımcı Hizmetler",
        "ÜE" to "Öğretim Elemanları",
        "ÖE" to "Öğretim Elemanları",
        "SP" to "Sözleşmeli Personel",
        "YARGI" to "Yargı Hizmetleri",
        "EİHS" to "Eğitim Öğretim Hizmetleri",
    )

    fun display(code: String): String {
        val label = names[code] ?: return code
        return "$code — $label"
    }

    fun shortLabel(code: String): String = names[code] ?: code
}
