package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * H sayfası formül matrisi — export_h_matrix.py çıktısı (h_formulas.json).
 */
class HFormulaRepository(context: Context) {

    private val root: HFormulasRoot by lazy {
        val json = context.assets.open("engine/h_formulas.json").bufferedReader().use { it.readText() }
        Gson().fromJson(json, HFormulasRoot::class.java)
    }

    private val rowByNumber: Map<Int, HFormulaRow> by lazy {
        root.rows.associateBy { it.r }
    }

    fun stats() = root.stats

    fun periodColumns(): List<HPeriodColumn> = root.periods

    fun coefficients(col: String): HPeriodColumn? =
        root.periods.find { it.col.equals(col, ignoreCase = true) }

    fun row(row: Int): HFormulaRow? = rowByNumber[row]

    /** [formula, cached] for a cell. */
    fun cell(row: Int, col: String): HCell? {
        val r = rowByNumber[row] ?: return null
        val raw = r.c?.get(col.uppercase()) ?: return null
        val f = raw.getOrNull(0)?.let { v -> if (v is String) v else v?.toString() }
        val c = (raw.getOrNull(1) as? Number)?.toDouble()
        if (f == null && c == null) return null
        return HCell(formula = f, cached = c)
    }

    fun cachedValue(row: Int, col: String): Double? = cell(row, col)?.cached

    companion object {
        const val COL_OCAK = "CN"
        const val COL_TEMMUZ = "CO"
        const val REF_OCAK_BRUT = 67826.62
        const val REF_TEMMUZ_BRUT = 72574.48
    }
}

data class HFormulasRoot(
    val version: Int,
    val periods: List<HPeriodColumn>,
    val rows: List<HFormulaRow>,
    val stats: HFormulaStats?,
)

data class HFormulaStats(
    val periodColumns: Int,
    val dataRows: Int,
    val formulaCells: Int,
)

data class HPeriodColumn(
    val col: String,
    @SerializedName("colIndex") val colIndex: Int,
    val year: Any?,
    val semester: String?,
    val memurK: Double?,
    val tabanK: Double?,
    val yanK: Double?,
    val enYuksek: Double?,
)

data class HFormulaRow(
    val r: Int,
    val label: String,
    /** [formula, cached] for BL column. */
    val bl: List<Any?>? = null,
    /** column letter → [formula, cached]. */
    val c: Map<String, List<Any?>>? = null,
)

data class HCell(
    val formula: String?,
    val cached: Double?,
)
