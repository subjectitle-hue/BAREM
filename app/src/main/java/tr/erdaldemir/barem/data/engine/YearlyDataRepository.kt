package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson

class YearlyDataRepository(context: Context) {

    private val gson = Gson()
    private val root: HEngineFullRoot by lazy {
        val json = context.assets.open("engine/h_engine_full.json")
            .bufferedReader().use { it.readText() }
        gson.fromJson(json, HEngineFullRoot::class.java)
    }

    fun yearlySeries(): List<YearlySeriesEntry> = root.yearlySeries

    fun fxByYear(): FxByYear = root.fxByYear

    fun activeYear(): Int = root.meta.activeYear

    fun coefficients(): SemesterCoeffs = root.coefficients

    fun gvMeta(): GvMeta = root.gvMeta

    fun seriesForYear(year: Int): YearlySeriesEntry? =
        root.yearlySeries.find { it.year == year }

    fun goldRate(year: Int): Double? =
        root.fxByYear.goldPerGram[year.toString()]

    fun usdRate(year: Int): Double? =
        root.fxByYear.usdRate[year.toString()]
}

data class HEngineFullRoot(
    val meta: HEngineMeta,
    val coefficients: SemesterCoeffs,
    val gvMeta: GvMeta,
    val yearlySeries: List<YearlySeriesEntry>,
    val fxByYear: FxByYear,
)

data class HEngineMeta(
    val activePeriod: String,
    val activeYear: Int,
    val semesterLabel: String,
    val ocakColumn: String? = null,
    val temmuzColumn: String? = null,
    val deductionMode: String? = null,
    val displayColumn: String? = null,
)

data class SemesterCoeffs(
    val ocak: CoeffSet,
    val temmuz: CoeffSet,
)

data class CoeffSet(
    val column: String,
    val memurK: Double,
    val tabanK: Double,
    val yanK: Double,
    val enYuksek: Double,
)

data class GvMeta(
    val agiOran: Double?,
    val agiAylik: Double?,
    val gvBracketsBm: List<Double?>,
    val gvBracketsBn: List<Double?>,
    val sgkTipi: String,
    val primOran5434: Double,
    val primOran5510: Double,
    val monthlyCached: List<MonthlyGvCached>,
    val deductionMode: String? = null,
    val crossColumnThresholds: CrossColumnThresholds? = null,
)

data class MonthlyGvCached(
    val month: String,
    val gvCached: Double?,
    val dvCached: Double?,
)

data class YearlySeriesEntry(
    val year: Int,
    val hColumn: String?,
    val monthlyNet: Map<String, Double>,
    val firstHalfAvg: Double?,
    val yearlyBrutGold: Double?,
    val yearlyNetGold: Double?,
    val yearlyBrutUsd: Double?,
    val yearlyNetUsd: Double?,
)

data class FxByYear(
    val goldPerGram: Map<String, @JvmSuppressWildcards Double>,
    val usdRate: Map<String, @JvmSuppressWildcards Double>,
)
