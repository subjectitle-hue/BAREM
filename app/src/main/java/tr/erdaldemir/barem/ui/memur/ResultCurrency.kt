package tr.erdaldemir.barem.ui.memur

import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.model.FxOutput
import tr.erdaldemir.barem.domain.model.SalaryResult

enum class ResultCurrency(val label: String) {
    TL("TL"),
    USD("Dolar"),
    EUR("Euro"),
    GOLD("Altın"),
    ;

    companion object {
        val selectorOrder = entries.toList()
    }
}

data class ResultPeriodStats(
    val firstHalf: Double,
    val secondHalf: Double,
    val yearlyAvg: Double,
)

object ResultCurrencyData {

    fun monthlyValues(result: SalaryResult, currency: ResultCurrency): List<Double> =
        when (currency) {
            ResultCurrency.TL -> result.monthlyNet
            ResultCurrency.USD -> result.fxOutput?.monthlyUsd.orEmpty()
            ResultCurrency.EUR -> result.fxOutput?.monthlyEur.orEmpty()
            ResultCurrency.GOLD -> result.fxOutput?.monthlyGoldQuarter.orEmpty()
        }

    fun periodStats(result: SalaryResult, currency: ResultCurrency): ResultPeriodStats =
        when (currency) {
            ResultCurrency.TL -> ResultPeriodStats(
                firstHalf = result.firstHalfAvg,
                secondHalf = result.secondHalfAvg,
                yearlyAvg = result.netAylik,
            )
            ResultCurrency.USD -> result.fxOutput?.toUsdStats()
                ?: ResultPeriodStats(0.0, 0.0, 0.0)
            ResultCurrency.EUR -> result.fxOutput?.toEurStats()
                ?: ResultPeriodStats(0.0, 0.0, 0.0)
            ResultCurrency.GOLD -> result.fxOutput?.toGoldStats()
                ?: ResultPeriodStats(0.0, 0.0, 0.0)
        }

    fun yearlyChartValue(row: YearlyCalcEngine.YearlyCalcRow, currency: ResultCurrency): Double? =
        when (currency) {
            ResultCurrency.TL -> (row.firstHalfAvg + row.secondHalfAvg) / 2.0
            ResultCurrency.USD -> row.fxOutput?.yearlyAvgUsd
            ResultCurrency.EUR -> row.fxOutput?.yearlyAvgEur
            ResultCurrency.GOLD -> row.fxOutput?.yearlyAvgGoldQuarter
        }

    private fun FxOutput.toUsdStats() = ResultPeriodStats(
        firstHalf = firstHalfUsd,
        secondHalf = secondHalfUsd,
        yearlyAvg = yearlyAvgUsd,
    )

    private fun FxOutput.toEurStats() = ResultPeriodStats(
        firstHalf = firstHalfEur,
        secondHalf = secondHalfEur,
        yearlyAvg = yearlyAvgEur,
    )

    private fun FxOutput.toGoldStats() = ResultPeriodStats(
        firstHalf = firstHalfGoldQuarter,
        secondHalf = secondHalfGoldQuarter,
        yearlyAvg = yearlyAvgGoldQuarter,
    )
}
