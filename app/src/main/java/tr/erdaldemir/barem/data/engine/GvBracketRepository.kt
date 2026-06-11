package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson

/** H!BM36–48 gelir vergisi dilimleri + damga oranı (export_gv_brackets.py). */
class GvBracketRepository(context: Context) {

    private val data: GvBrackets by lazy {
        val json = context.assets.open("engine/gv_brackets.json").bufferedReader().use { it.readText() }
        Gson().fromJson(json, GvBrackets::class.java)
    }

    fun brackets(): GvBrackets = data
}

data class GvBrackets(
    val dilimLimits: List<Double>,
    val dilimBaseTax: List<Double>,
    val dilimRates: List<Double>,
    val damgaRate: Double,
    val damgaMuafiyetBm: Double?,
    val agiMonthly: Double,
    val deductionMode: String? = null,
    val crossColumnThresholds: CrossColumnThresholds? = null,
)

data class CrossColumnThresholds(
    val gvFirstHalf: List<Double>,
    val gvSecondHalf: List<Double>,
    val dvMuafOcak: Double,
    val dvMuafTem: Double,
)
