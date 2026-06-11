package tr.erdaldemir.barem.domain.model

/**
 * TS (meslek) ve M (kadro) sayfalarındaki hizmet sınıfı kodları birebir aynı değil.
 * Excel'deki eşleştirmeye göre kadro aramasında M kodu kullanılır.
 */
object HizmetSinifiMapper {

    private val tsToKadro = mapOf(
        "EÖS" to "EÖHS",
        "ÜE" to "EHS",
        "EİHS" to "EÖHS",
    )

    /** M sayfasındaki kadro hizmet sınıfı kodu. */
    fun kadroSinifi(tsSinifi: String): String = tsToKadro[tsSinifi] ?: tsSinifi

    /** SP (sözleşmeli) kadrosu M'de yok; GİH kadroları listelenir. */
    fun kadroSinifiForLookup(tsSinifi: String): String =
        if (tsSinifi == "SP") "GİH" else kadroSinifi(tsSinifi)
}
