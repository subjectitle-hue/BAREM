package tr.erdaldemir.barem.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tr.erdaldemir.barem.domain.model.AFormOptions
import tr.erdaldemir.barem.domain.model.HizmetSinifiMapper
import tr.erdaldemir.barem.domain.model.MKadroRow
import tr.erdaldemir.barem.domain.model.TsMeslekRow

/**
 * Memur katalog — veri dosyalarından (`ts_meslek.json`, `m_kadro.json`).
 * Nihai sürümde yalnızca JSON satır sayısı artar; uygulama akışı değişmez.
 */
class MemurCatalogRepository(context: Context) {

    private val gson = Gson()

    private val tsRows: List<TsMeslekRow> by lazy {
        loadList(context, "memur/ts_meslek.json")
    }

    private val mRows: List<MKadroRow> by lazy {
        loadList(context, "memur/m_kadro.json")
    }

    private val tsByKoluSinifi: Map<String, List<TsMeslekRow>> by lazy {
        tsRows.groupBy { meslekKey(it.hizmetKolu, it.hizmetSinifi) }
    }

    private val mRowsByKadroSinifi: Map<String, List<MKadroRow>> by lazy {
        mRows.groupBy { it.hizmetSinifi }
    }

    private val aFormOptions: AFormOptions by lazy {
        val json = context.assets.open("memur/a_form_options.json").bufferedReader().use { it.readText() }
        gson.fromJson(json, AFormOptions::class.java)
    }

    fun meslekCount(): Int = tsRows.size

    fun hizmetKolleri(): List<String> =
        tsRows.map { it.hizmetKolu }.distinct().sorted()

    fun hizmetSiniflari(hizmetKolu: String): List<String> =
        tsRows.filter { it.hizmetKolu == hizmetKolu }
            .map { it.hizmetSinifi }
            .distinct()
            .sorted()

    /** v1-Uygulama.xlsx MEMUR sayfası sırası; katalogda olanlar listelenir. */
    fun memurHizmetSiniflariOrdered(): List<String> {
        val available = tsRows.map { it.hizmetSinifi }.distinct().toSet()
        val ordered = MEMUR_SINIF_EXCEL_ORDER.filter { it in available }
        val rest = available.filter { it !in MEMUR_SINIF_EXCEL_ORDER }.sorted()
        return ordered + rest
    }

    fun resolveHizmetKolu(hizmetSinifi: String): String? =
        tsRows
            .filter { it.hizmetSinifi == hizmetSinifi }
            .groupingBy { it.hizmetKolu }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

    fun meslekler(hizmetKolu: String, hizmetSinifi: String): List<TsMeslekRow> =
        tsByKoluSinifi[meslekKey(hizmetKolu, hizmetSinifi)].orEmpty()

    /** Tek meslek varsa veya unvan eşleşmesi bulunursa otomatik atama. */
    fun resolveMeslek(hizmetKolu: String, hizmetSinifi: String, unvan: String?): TsMeslekRow? {
        val list = meslekler(hizmetKolu, hizmetSinifi)
        if (list.isEmpty()) return null
        if (list.size == 1) return list.first()
        if (!unvan.isNullOrBlank()) {
            list.find { row ->
                row.detay.contains(unvan, ignoreCase = true) ||
                    unvan.contains(row.kodPrefix, ignoreCase = true)
            }?.let { return it }
        }
        return null
    }

    fun searchMeslekler(
        hizmetKolu: String,
        hizmetSinifi: String,
        query: String,
    ): List<TsMeslekRow> {
        val base = meslekler(hizmetKolu, hizmetSinifi)
        val q = query.trim().lowercase()
        if (q.isEmpty()) return base
        return base.filter { row ->
            row.kod.lowercase().contains(q) ||
                row.detay.lowercase().contains(q) ||
                row.displayLabel.lowercase().contains(q)
        }
    }

    fun unvanlar(hizmetSinifi: String): List<String> {
        val kadroSinifi = HizmetSinifiMapper.kadroSinifiForLookup(hizmetSinifi)
        return mRowsByKadroSinifi[kadroSinifi].orEmpty()
            .map { it.unvan }
            .distinct()
            .sorted()
    }

    fun searchUnvanlar(hizmetSinifi: String, query: String): List<String> {
        val base = unvanlar(hizmetSinifi)
        val q = query.trim()
        if (q.isEmpty()) return base
        return base.filter { it.contains(q, ignoreCase = true) }
    }

    fun detaylar(hizmetSinifi: String, unvan: String): List<String> {
        val kadroSinifi = HizmetSinifiMapper.kadroSinifiForLookup(hizmetSinifi)
        return mRowsByKadroSinifi[kadroSinifi].orEmpty()
            .filter { it.unvan == unvan }
            .map { it.detay }
            .distinct()
            .sortedBy { if (it.isBlank()) " " else it }
    }

    fun dereceler(hizmetSinifi: String, unvan: String, detay: String?): List<Int> {
        val kadroSinifi = HizmetSinifiMapper.kadroSinifiForLookup(hizmetSinifi)
        return mRowsByKadroSinifi[kadroSinifi].orEmpty()
            .filter {
                it.unvan == unvan &&
                    (detay == null || it.detay == detay) &&
                    it.derece != null
            }
            .mapNotNull { it.derece }
            .distinct()
            .sorted()
    }

    fun kadroSecenekleri(hizmetSinifi: String): List<MKadroRow> {
        val kadroSinifi = HizmetSinifiMapper.kadroSinifiForLookup(hizmetSinifi)
        return mRowsByKadroSinifi[kadroSinifi].orEmpty()
            .distinctBy { Triple(it.unvan, it.detay, it.derece) }
    }

    fun findKadro(
        hizmetSinifi: String,
        unvan: String,
        detay: String,
        derece: Int?,
    ): MKadroRow? {
        val kadroSinifi = HizmetSinifiMapper.kadroSinifiForLookup(hizmetSinifi)
        return mRowsByKadroSinifi[kadroSinifi].orEmpty().find {
            it.unvan == unvan &&
                it.detay == detay &&
                it.derece == derece
        }
    }

    fun medeniHalSecenekleri(): List<String> = aFormOptions.medeniHal

    fun topluSozlesmeSecenekleri(): List<String> = aFormOptions.topluSozlesme

    fun yabanciDilSecenekleri(): List<String> = aFormOptions.yabanciDil

    private fun meslekKey(kolu: String, sinifi: String): String = "$kolu\u0000$sinifi"

    companion object {
        private val MEMUR_SINIF_EXCEL_ORDER = listOf(
            "GİH",
            "AHS",
            "SHS",
            "THS",
            "DHS",
            "EHS",
            "EÖHS",
            "EÖS",
            "MİAH",
            "YHS",
            "ÜE",
            "ÖE",
            "SP",
        )
    }

    private inline fun <reified T> loadList(context: Context, assetPath: String): List<T> {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(json, type)
    }
}
