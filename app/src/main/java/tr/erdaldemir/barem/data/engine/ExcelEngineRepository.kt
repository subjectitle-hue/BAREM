package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tr.erdaldemir.barem.domain.model.HizmetSinifiMapper

class ExcelEngineRepository(context: Context) {

    private val gson = Gson()
    private val appContext = context.applicationContext

    private val indexEntries: List<IndexEntry> by lazy {
        val json = appContext.assets.open("engine/index_table.json").bufferedReader().use { it.readText() }
        val root = gson.fromJson(json, IndexTableRoot::class.java)
        root.entries
    }

    private val vTable: Map<String, Map<String, Double>> by lazy {
        val json = appContext.assets.open("engine/v_derece_kademe.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
        gson.fromJson(json, type)
    }

    private val mKadroFull: List<MKadroFullRow> by lazy {
        val json = appContext.assets.open("engine/m_kadro_full.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<MKadroFullRow>>() {}.type
        gson.fromJson(json, type)
    }

    private val engineFull: HEngineFullRoot by lazy {
        val json = appContext.assets.open("engine/h_engine_full.json")
            .bufferedReader().use { it.readText() }
        gson.fromJson(json, HEngineFullRoot::class.java)
    }

    private val fxRates: Map<String, Map<String, Double>> by lazy {
        val json = appContext.assets.open("engine/fx_rates.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
        gson.fromJson(json, type)
    }

    private val periodResolver: PeriodColumnResolver by lazy {
        PeriodColumnResolver(appContext)
    }

    private val mLookup: MHLookupRepository by lazy {
        MHLookupRepository(appContext)
    }

    private val tsIndicators: Map<String, Map<String, Double>> by lazy {
        val json = appContext.assets.open("engine/ts_indicator_by_kod.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
        gson.fromJson(json, type)
    }

    fun activePeriod(): String = engineFull.meta.activePeriod

    fun activeYear(): Int = engineFull.meta.activeYear

    fun periodYear(period: String): String =
        period.substringBefore("-").ifBlank { engineFull.meta.activeYear.toString() }

    fun semesterCoefficients(): SemesterCoeffs = engineFull.coefficients

    fun activeSemesterPair(): SemesterColumnPair = periodResolver.activeYearPair()

    fun semesterPairForYear(year: Int): SemesterColumnPair? =
        periodResolver.semesterColumnsForYear(year)

    fun gvMeta(): GvMeta = engineFull.gvMeta

    fun indexLookup(label: String, period: String): Double {
        val entry = indexEntries.find { it.label.equals(label, ignoreCase = true) }
            ?: return 0.0
        val raw = entry.values[period] ?: return 0.0
        return when (raw) {
            is Double -> raw
            is Number -> raw.toDouble()
            is String -> raw.replace(",", ".").toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    fun vLookupDereceKademe(derece: Int, kademe: Int, year: String): Double {
        val key = "$derece/$kademe"
        return vTable[key]?.get(year) ?: 0.0
    }

    fun vLookupTable(key: String, year: String): Double =
        vTable[key]?.get(year) ?: 0.0

    fun ekGostergePoints(ekCode: String?, derece: Int, year: String): Double {
        if (ekCode.isNullOrBlank()) return 0.0
        return vLookupTable("$ekCode-$derece", year)
    }

    /**
     * H!BL72/78/81 — VLOOKUP(CONCATENATE(kod,"-",derece), V) veya doğrudan kod.
     */
    fun vLookupPayIndicator(code: String?, derece: Int, year: String): Double {
        if (code.isNullOrBlank()) return 0.0
        vLookupTable("$code-$derece", year).takeIf { it > 0 }?.let { return it }
        return vLookupTable(code, year)
    }

    fun tsPrimeIndicator(meslekKod: String?, year: String): Double {
        if (meslekKod.isNullOrBlank()) return 0.0
        return tsIndicators[meslekKod]?.get(year) ?: 0.0
    }

    fun tsPrimeIndicatorForKadro(
        unvan: String?,
        detay: String?,
        derece: Int?,
        year: String,
    ): Double {
        if (unvan.isNullOrBlank() || derece == null) return 0.0
        val row = mLookup.find(unvan, detay.orEmpty(), derece) ?: return 0.0
        val kod = row.tsPrimeKod ?: return 0.0
        return tsPrimeIndicator(kod, year)
    }

    fun ilaveOdemeGostergesi(year: String, hasTazminatPath: Boolean): Double {
        if (!hasTazminatPath) return 0.0
        for (key in listOf("İlave Ödeme", "Ilave Odeme", "İlave Ödeme ")) {
            val v = vLookupTable(key, year)
            if (v > 0) return v
        }
        return vTable.entries.firstOrNull { it.key.contains("lave", ignoreCase = true) && it.key.contains("deme", ignoreCase = true) }
            ?.value?.get(year) ?: 0.0
    }

    fun mLookupForKadro(unvan: String?, detay: String?, derece: Int?): MHLookupRow? {
        if (unvan.isNullOrBlank() || derece == null) return null
        return mLookup.find(unvan, detay.orEmpty(), derece)
    }

    fun findKadroFull(
        tsHizmetSinifi: String,
        unvan: String,
        detay: String,
        derece: Int?,
    ): MKadroFullRow? {
        val kadroSinifi = HizmetSinifiMapper.kadroSinifiForLookup(tsHizmetSinifi)
        return mKadroFull.find {
            it.hizmetSinifi == kadroSinifi &&
                it.unvan == unvan &&
                it.detay == detay &&
                it.derece == derece
        }
    }

    fun inflationRate(year: Int): Double? {
        val pair = semesterPairForYear(year) ?: return null
        val period = "$year-${pair.ocakCoeffs.memurK}"
        val v = indexLookup("ENFLASYON", period)
        return v.takeIf { it > 0 }
    }

    /** Index tablosundan yıl ve döneme göre net asgari ücret (TL). */
    fun netAsgariUcret(year: Int, secondHalf: Boolean = false): Double? {
        val pair = semesterPairForYear(year) ?: return null
        val coeffs = if (secondHalf) pair.temmuzCoeffs else pair.ocakCoeffs
        val period = "$year-${coeffs.memurK}"
        return indexLookup("Net Asgari Ücret", period).takeIf { it > 0 }
    }

    fun goldRate(period: String): Double {
        val year = periodYear(period).toIntOrNull() ?: engineFull.meta.activeYear
        return fxRates.entries.firstOrNull { it.key.contains("Altın", ignoreCase = true) }
            ?.value?.get(period)
            ?: engineFull.fxByYear.goldPerGram[year.toString()]
            ?: indexLookup("Çeyrek Altın", period)
    }

    fun usdRate(period: String): Double {
        val year = periodYear(period).toIntOrNull() ?: engineFull.meta.activeYear
        return fxRates.entries.firstOrNull { it.key.contains("Dolar", ignoreCase = true) }
            ?.value?.get(period)
            ?: engineFull.fxByYear.usdRate[year.toString()]
            ?: 0.0
    }

    fun tazminatOrani(meslekKod: String?): Double =
        resolveOran(meslekKod)?.tazminat ?: 0.0

    fun yanOdemeGostergesi(meslekKod: String?): Double =
        resolveOran(meslekKod)?.yanOdeme ?: 0.0

    fun ekOdemeGostergesi(meslekKod: String?): Double =
        resolveOran(meslekKod)?.ekOdeme ?: 0.0

    private fun resolveOran(meslekKod: String?): MeslekOranlari? {
        if (meslekKod.isNullOrBlank()) return null
        meslekOranlari[meslekKod]?.let { return it }
        val prefix = meslekKod.substringBeforeLast("-")
        return meslekOranlari.entries.firstOrNull { (k, _) ->
            k.startsWith(prefix) || prefix.startsWith(k.substringBeforeLast("-"))
        }?.value
    }

    private val meslekOranlari: Map<String, MeslekOranlari> by lazy {
        runCatching {
            val json = appContext.assets.open("engine/ts_meslek_oran.json")
                .bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, MeslekOranlari>>() {}.type
            gson.fromJson<Map<String, MeslekOranlari>>(json, type)
        }.getOrDefault(emptyMap())
    }

}

data class MeslekOranlari(
    val tazminat: Double,
    val yanOdeme: Double,
    val ekOdeme: Double,
)

data class IndexTableRoot(val entries: List<IndexEntry>)

data class IndexEntry(
    val label: String,
    val values: Map<String, @JvmSuppressWildcards Any>,
)

data class MKadroFullRow(
    val hizmetSinifi: String,
    val barem: Int?,
    val unvan: String,
    val detay: String,
    val derece: Int?,
    val taban: Double,
    val kidem: Double,
    val ekGosterge: String,
)

