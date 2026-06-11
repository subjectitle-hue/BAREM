package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.FxSemesterRates
import tr.erdaldemir.barem.domain.model.FxOutput
import kotlin.math.round

object FxOutputCalculator {

    fun compute(monthlyNet: List<Double>, rates: FxSemesterRates): FxOutput? {
        if (monthlyNet.size < 12) return null
        val usd = monthlyNet.mapIndexed { i, net ->
            convert(net, if (i < 6) rates.usdOcak else rates.usdTemmuz)
        }
        val eur = monthlyNet.mapIndexed { i, net ->
            convert(net, if (i < 6) rates.eurOcak else rates.eurTemmuz)
        }
        val gold = monthlyNet.map { net -> convert(net, rates.goldQuarter) }
        return FxOutput(
            monthlyUsd = usd,
            monthlyEur = eur,
            monthlyGoldQuarter = gold,
            firstHalfUsd = r2(usd.take(6).average()),
            secondHalfUsd = r2(usd.drop(6).average()),
            yearlyAvgUsd = r2(usd.average()),
            firstHalfEur = r2(eur.take(6).average()),
            secondHalfEur = r2(eur.drop(6).average()),
            yearlyAvgEur = r2(eur.average()),
            firstHalfGoldQuarter = r2(gold.take(6).average()),
            secondHalfGoldQuarter = r2(gold.drop(6).average()),
            yearlyAvgGoldQuarter = r2(gold.average()),
        )
    }

    private fun convert(net: Double, rate: Double?): Double {
        if (rate == null || rate <= 0) return 0.0
        return r2(net / rate)
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
