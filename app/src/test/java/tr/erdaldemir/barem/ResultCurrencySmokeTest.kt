package tr.erdaldemir.barem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tr.erdaldemir.barem.domain.model.FxOutput
import tr.erdaldemir.barem.domain.model.SalaryResult
import tr.erdaldemir.barem.ui.memur.ResultCurrency
import tr.erdaldemir.barem.ui.memur.ResultCurrencyData

class ResultCurrencySmokeTest {

    private val sampleResult = SalaryResult(
        brutAylik = 72_574.48,
        kesintiler = 10_000.0,
        netAylik = 64_357.10,
        cocukYardimi = 0.0,
        toplamNet = 64_357.10,
        yillikBrut = 842_406.60,
        yillikNet = 772_285.23,
        altinGramKarsilik = 1.0,
        dolarKarsilik = 2_000.0,
        firstHalfAvg = 62_189.25,
        secondHalfAvg = 66_524.95,
        periodLabel = "2026",
        monthlyNet = List(6) { 62_189.25 } + List(6) { 66_524.95 },
        fxOutput = FxOutput(
            monthlyUsd = List(12) { 1_800.0 },
            monthlyEur = List(12) { 1_650.0 },
            monthlyGoldQuarter = List(12) { 0.45 },
            firstHalfUsd = 1_750.0,
            secondHalfUsd = 1_850.0,
            yearlyAvgUsd = 1_800.0,
            firstHalfEur = 1_600.0,
            secondHalfEur = 1_700.0,
            yearlyAvgEur = 1_650.0,
            firstHalfGoldQuarter = 0.42,
            secondHalfGoldQuarter = 0.48,
            yearlyAvgGoldQuarter = 0.45,
        ),
    )

    @Test
    fun monthlyValues_returnsTwelveMonthsForEachCurrency() {
        ResultCurrency.selectorOrder.forEach { currency ->
            val values = ResultCurrencyData.monthlyValues(sampleResult, currency)
            assertEquals("month count for $currency", 12, values.size)
            assertTrue("positive sample for $currency", values.all { it > 0 })
        }
    }

    @Test
    fun periodStats_matchesSalaryResultForTl() {
        val stats = ResultCurrencyData.periodStats(sampleResult, ResultCurrency.TL)
        assertEquals(sampleResult.firstHalfAvg, stats.firstHalf, 0.01)
        assertEquals(sampleResult.secondHalfAvg, stats.secondHalf, 0.01)
        assertEquals(sampleResult.netAylik, stats.yearlyAvg, 0.01)
    }

    @Test
    fun periodStats_readsFxOutputForForeignCurrencies() {
        val usd = ResultCurrencyData.periodStats(sampleResult, ResultCurrency.USD)
        assertEquals(1_750.0, usd.firstHalf, 0.01)
        assertEquals(1_850.0, usd.secondHalf, 0.01)
        assertEquals(1_800.0, usd.yearlyAvg, 0.01)
    }
}
