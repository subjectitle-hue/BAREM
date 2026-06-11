package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson

class FxRateRepository(context: Context) {

    private val byYear: Map<String, FxSemesterRates> by lazy {
        val json = context.assets.open("engine/fx_semester_rates.json").bufferedReader().use { it.readText() }
        val root = Gson().fromJson(json, FxSemesterRatesRoot::class.java)
        root.byYear
    }

    fun ratesForYear(year: Int): FxSemesterRates? = byYear[year.toString()]
}

data class FxSemesterRatesRoot(val byYear: Map<String, FxSemesterRates>)

data class FxSemesterRates(
    val usdOcak: Double?,
    val usdTemmuz: Double?,
    val eurOcak: Double?,
    val eurTemmuz: Double?,
    val goldQuarter: Double?,
    val column: String? = null,
)
