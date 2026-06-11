package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson

/** A/H dönem eşlemesi — yıl → Ocak/Temmuz katsayı sütunları (h_period_map.json). */
class PeriodColumnResolver(context: Context) {

    private val periods: List<PeriodMapEntry> by lazy {
        val json = context.assets.open("engine/h_period_map.json").bufferedReader().use { it.readText() }
        Gson().fromJson(json, PeriodMapRoot::class.java).periods
    }

    fun semesterColumnsForYear(year: Int): SemesterColumnPair? {
        val veriIdx = periods.indexOfFirst { entry ->
            entry.year == year && entry.semester.equals("VERİ", ignoreCase = true)
        }
        if (veriIdx < 0) return null
        val ocak = periods.drop(veriIdx + 1).firstOrNull {
            it.semester.equals("OCAK", ignoreCase = true)
        } ?: return null
        val temmuz = periods.drop(veriIdx + 1).firstOrNull {
            it.semester.equals("TEMMUZ", ignoreCase = true)
        } ?: return null
        return SemesterColumnPair(
            ocak = ocak.col,
            temmuz = temmuz.col,
            ocakCoeffs = ocak.toCoeffSet(),
            temmuzCoeffs = temmuz.toCoeffSet(),
        )
    }

    fun activeYearPair(): SemesterColumnPair {
        return semesterColumnsForYear(2026)
            ?: SemesterColumnPair(
                ocak = "CN",
                temmuz = "CO",
                ocakCoeffs = CoeffSet("CN", 1.387871, 22.722793, 0.440141, 13184.774500000001),
                temmuzCoeffs = CoeffSet("CO", 1.4850219700000002, 24.31338851, 0.460351, 14068.0),
            )
    }

    /** H sayfasında katsayısı olan yıllar (1997…active). */
    fun availableYears(activeYear: Int = 2026): List<Int> =
        periods.mapNotNull { it.year }.distinct().sorted().filter { it in 1997..activeYear }

    private fun PeriodMapEntry.toCoeffSet(): CoeffSet = CoeffSet(
        column = col,
        memurK = memurK ?: 0.0,
        tabanK = tabanK ?: 0.0,
        yanK = yanK ?: 0.0,
        enYuksek = enYuksek ?: 0.0,
    )
}

data class PeriodMapRoot(val periods: List<PeriodMapEntry>)

data class PeriodMapEntry(
    val col: String,
    val year: Int?,
    val semester: String?,
    val memurK: Double?,
    val tabanK: Double?,
    val yanK: Double?,
    val enYuksek: Double?,
)

data class SemesterColumnPair(
    val ocak: String,
    val temmuz: String,
    val ocakCoeffs: CoeffSet,
    val temmuzCoeffs: CoeffSet,
)
