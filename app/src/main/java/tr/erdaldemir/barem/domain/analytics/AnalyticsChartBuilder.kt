package tr.erdaldemir.barem.domain.analytics

import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.model.SalaryResult
import kotlin.math.round

object AnalyticsChartBuilder {

    fun build(
        result: SalaryResult?,
        yearly: List<YearlyCalcEngine.YearlyCalcRow>?,
        engineRepo: ExcelEngineRepository,
    ): AnalyticsChartBundle? {
        if (result == null) return null
        val rows = yearly.orEmpty().sortedBy { it.year }
        if (rows.isEmpty()) {
            return singleYearBundle(result, engineRepo)
        }
        val tl = rows.map { YearPoint(it.year, yearlyAvgNet(it)) }
        val usd = rows.mapNotNull { row ->
            row.fxOutput?.yearlyAvgUsd?.let { YearPoint(row.year, it) }
        }
        val eur = rows.mapNotNull { row ->
            row.fxOutput?.yearlyAvgEur?.let { YearPoint(row.year, it) }
        }
        val gold = rows.mapNotNull { row ->
            row.fxOutput?.yearlyAvgGoldQuarter?.let { YearPoint(row.year, it) }
        }
        val inflationVsSalary = buildInflationVsSalary(rows, engineRepo)
        val deductions = rows.mapNotNull { row ->
            row.deductionBreakdown?.let { d ->
                DeductionYearPoint(
                    year = row.year,
                    netPct = d.netPct,
                    primPct = d.primPct,
                    gvPct = d.gvPct,
                    dvPct = d.dvPct,
                )
            }
        }
        val activeYear = rows.last().year
        return AnalyticsChartBundle(
            tlSeries = tl,
            usdSeries = usd,
            eurSeries = eur,
            goldSeries = gold,
            inflationVsSalary = inflationVsSalary,
            deductionRates = deductions,
            activeYear = activeYear,
            activeYearNet = tl.lastOrNull()?.value,
            periodLabel = result.periodLabel,
        )
    }

    private fun singleYearBundle(
        result: SalaryResult,
        engineRepo: ExcelEngineRepository,
    ): AnalyticsChartBundle {
        val year = engineRepo.activeYear()
        val avgNet = result.netAylik
        val tl = listOf(YearPoint(year, avgNet))
        val fx = result.fxOutput
        val usd = fx?.yearlyAvgUsd?.let { listOf(YearPoint(year, it)) }.orEmpty()
        val eur = fx?.yearlyAvgEur?.let { listOf(YearPoint(year, it)) }.orEmpty()
        val gold = fx?.yearlyAvgGoldQuarter?.let { listOf(YearPoint(year, it)) }.orEmpty()
        val inflation = engineRepo.inflationRate(year)?.let { inf ->
            listOf(DualYearPoint(year, inf, 0.0))
        }.orEmpty()
        val deductions = result.deductionBreakdown?.let { d ->
            listOf(
                DeductionYearPoint(
                    year = year,
                    netPct = d.netPct,
                    primPct = d.primPct,
                    gvPct = d.gvPct,
                    dvPct = d.dvPct,
                ),
            )
        }.orEmpty()
        return AnalyticsChartBundle(
            tlSeries = tl,
            usdSeries = usd,
            eurSeries = eur,
            goldSeries = gold,
            inflationVsSalary = inflation,
            deductionRates = deductions,
            activeYear = year,
            activeYearNet = avgNet,
            periodLabel = result.periodLabel,
        )
    }

    private fun yearlyAvgNet(row: YearlyCalcEngine.YearlyCalcRow): Double =
        r2((row.firstHalfAvg + row.secondHalfAvg) / 2.0)

    private fun buildInflationVsSalary(
        rows: List<YearlyCalcEngine.YearlyCalcRow>,
        engineRepo: ExcelEngineRepository,
    ): List<DualYearPoint> {
        val nets = rows.associate { it.year to yearlyAvgNet(it) }
        return rows.mapNotNull { row ->
            val inflation = engineRepo.inflationRate(row.year) ?: return@mapNotNull null
            val prevNet = nets[row.year - 1]
            val currentNet = nets[row.year] ?: return@mapNotNull null
            val salaryGrowth = if (prevNet != null && prevNet > 0) {
                r2((currentNet - prevNet) / prevNet * 100.0)
            } else {
                0.0
            }
            DualYearPoint(row.year, inflation, salaryGrowth)
        }
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
