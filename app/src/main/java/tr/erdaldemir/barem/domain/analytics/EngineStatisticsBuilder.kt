package tr.erdaldemir.barem.domain.analytics

import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.PeriodColumnResolver
import tr.erdaldemir.barem.data.engine.YearlyDataRepository
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine

data class YearValueRow(
    val year: Int,
    val yearlyValue: Double,
    val monthlyValue: Double = yearlyValue / 12.0,
)

object EngineStatisticsBuilder {
    private const val YEAR_START = 2000
    private const val YEAR_END = 2026

    fun years(activeYear: Int): List<Int> =
        (YEAR_START..minOf(YEAR_END, activeYear)).toList()

    fun usdRates(repo: YearlyDataRepository): List<YearValueRow> =
        years(repo.activeYear()).mapNotNull { year ->
            repo.usdRate(year)?.let { YearValueRow(year, it, it) }
        }

    fun goldPerGram(repo: YearlyDataRepository): List<YearValueRow> =
        years(repo.activeYear()).mapNotNull { year ->
            repo.goldRate(year)?.let { YearValueRow(year, it, it) }
        }

    fun netAsgari(excel: ExcelEngineRepository, activeYear: Int): List<YearValueRow> =
        years(activeYear).mapNotNull { year ->
            val ocak = excel.netAsgariUcret(year, secondHalf = false) ?: return@mapNotNull null
            val temmuz = excel.netAsgariUcret(year, secondHalf = true) ?: ocak
            val avg = (ocak + temmuz) / 2.0
            YearValueRow(year, avg, avg)
        }

    fun asgariRaisePct(rows: List<YearValueRow>): List<YearValueRow> {
        if (rows.size < 2) return emptyList()
        return rows.zipWithNext { prev, next ->
            val pct = if (prev.yearlyValue > 0) {
                (next.yearlyValue - prev.yearlyValue) / prev.yearlyValue * 100.0
            } else {
                0.0
            }
            YearValueRow(next.year, pct, pct)
        }
    }

    fun memurRaisePct(
        yearly: List<YearlyCalcEngine.YearlyCalcRow>,
        engine: ExcelEngineRepository,
    ): List<YearValueRow> {
        val nets = yearly
            .filter { it.year in YEAR_START..YEAR_END }
            .sortedBy { it.year }
            .map { row ->
                val avg = row.monthlyNet.takeIf { it.isNotEmpty() }?.average()
                    ?: (row.firstHalfAvg + row.secondHalfAvg) / 2.0
                YearValueRow(row.year, avg, avg)
            }
        if (nets.size < 2) return emptyList()
        return nets.zipWithNext { prev, next ->
            val pct = if (prev.yearlyValue > 0) {
                (next.yearlyValue - prev.yearlyValue) / prev.yearlyValue * 100.0
            } else {
                0.0
            }
            YearValueRow(next.year, pct, pct)
        }
    }

    fun inflationSeries(engine: ExcelEngineRepository, activeYear: Int): List<YearValueRow> =
        years(activeYear).mapNotNull { year ->
            engine.inflationRate(year)?.let { YearValueRow(year, it, it) }
        }

    fun primRates(repo: YearlyDataRepository): List<Pair<String, Double>> {
        val meta = repo.gvMeta()
        return listOf(
            "5434" to meta.primOran5434,
            "5510" to meta.primOran5510,
        )
    }

    fun gvBracketLabels(): List<String> = listOf("1. dilim", "2. dilim", "3. dilim", "4. dilim", "5. dilim")

    fun gvBrackets(repo: YearlyDataRepository): List<Double?> =
        repo.gvMeta().gvBracketsBn

    fun dollarFromSalary(yearly: List<YearlyCalcEngine.YearlyCalcRow>): List<YearValueRow> =
        yearly
            .filter { it.year in YEAR_START..YEAR_END }
            .mapNotNull { row ->
                row.fxOutput?.yearlyAvgUsd?.let { YearValueRow(row.year, it, it / 12.0) }
            }

    fun goldFromSalary(yearly: List<YearlyCalcEngine.YearlyCalcRow>): List<YearValueRow> =
        yearly
            .filter { it.year in YEAR_START..YEAR_END }
            .mapNotNull { row ->
                row.fxOutput?.yearlyAvgGoldQuarter?.let { YearValueRow(row.year, it, it / 12.0) }
            }
}
