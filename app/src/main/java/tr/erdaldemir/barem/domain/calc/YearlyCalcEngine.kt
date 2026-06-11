package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.FxRateRepository
import tr.erdaldemir.barem.data.engine.GvBracketRepository
import tr.erdaldemir.barem.data.engine.MKadroFullRow
import tr.erdaldemir.barem.data.engine.PeriodColumnResolver
import tr.erdaldemir.barem.data.engine.YearlyDataRepository
import tr.erdaldemir.barem.data.engine.YearlySeriesEntry
import tr.erdaldemir.barem.domain.model.FxOutput
import tr.erdaldemir.barem.domain.model.DeductionBreakdown
import tr.erdaldemir.barem.domain.model.MemurFormState

/**
 * Faz 6 — Seçilen kadroya göre 1997–2026 yıllık net serisi.
 * Her yıl için H dönem sütunları (Ocak/Temmuz) ile ExcelMemurEngine çalıştırılır.
 */
class YearlyCalcEngine(
    private val yearlyRepo: YearlyDataRepository,
    private val periodResolver: PeriodColumnResolver,
    private val memurEngine: ExcelMemurEngine,
    private val excelRepo: ExcelEngineRepository,
    private val fxRates: FxRateRepository,
) {

    data class YearlyCalcRow(
        val year: Int,
        val monthlyNet: List<Double>,
        val yillikNet: Double,
        val yillikBrut: Double,
        val firstHalfAvg: Double,
        val secondHalfAvg: Double,
        val goldPerGram: Double?,
        val usdRate: Double?,
        val excelGold: Double?,
        val excelUsd: Double?,
        val fxOutput: FxOutput? = null,
        val deductionBreakdown: DeductionBreakdown? = null,
    )

    fun calculateSeries(form: MemurFormState, kadro: MKadroFullRow?): List<YearlyCalcRow> {
        val staticSeries = yearlyRepo.yearlySeries().associateBy { it.year }
        val activeYear = yearlyRepo.activeYear()
        val years = periodResolver.availableYears(activeYear)
        return years.mapNotNull { year ->
            val pair = periodResolver.semesterColumnsForYear(year) ?: return@mapNotNull null
            val result = memurEngine.calculateForPeriod(form, kadro, year, pair)
            val gold = yearlyRepo.goldRate(year)
            val usd = yearlyRepo.usdRate(year)
            val ref = staticSeries[year]
            YearlyCalcRow(
                year = year,
                monthlyNet = result.monthlyNet,
                yillikNet = result.yillikNet,
                yillikBrut = result.yillikBrut,
                firstHalfAvg = result.firstHalfAvg,
                secondHalfAvg = result.secondHalfAvg,
                goldPerGram = gold,
                usdRate = usd,
                excelGold = ref?.yearlyNetGold,
                excelUsd = ref?.yearlyNetUsd,
                fxOutput = result.fxOutput,
                deductionBreakdown = result.deductionBreakdown,
            )
        }
    }

    fun referenceSeriesForYear(year: Int): YearlySeriesEntry? =
        yearlyRepo.seriesForYear(year)
}
