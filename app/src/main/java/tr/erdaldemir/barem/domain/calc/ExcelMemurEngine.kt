package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.FxRateRepository
import tr.erdaldemir.barem.data.engine.GvBracketRepository
import tr.erdaldemir.barem.data.engine.HFormulaRepository
import tr.erdaldemir.barem.data.engine.MKadroFullRow
import tr.erdaldemir.barem.data.engine.SemesterColumnPair
import tr.erdaldemir.barem.data.engine.SemesterCoeffs
import tr.erdaldemir.barem.domain.model.DeductionBreakdown
import tr.erdaldemir.barem.domain.model.MemurFormState
import tr.erdaldemir.barem.domain.model.SalaryResult
import kotlin.math.round

class ExcelMemurEngine(
    private val repo: ExcelEngineRepository,
    private val formulas: HFormulaRepository,
    private val gvBrackets: GvBracketRepository,
    private val fxRates: FxRateRepository,
    private val blResolver: BlResolver = BlResolver(repo),
    private val deductionEngine: HDeductionEngine = HDeductionEngine(gvBrackets),
) {

    fun calculate(form: MemurFormState, kadro: MKadroFullRow?): SalaryResult {
        val period = repo.activePeriod()
        val pair = repo.activeSemesterPair()
        return calculateForPeriod(form, kadro, repo.periodYear(period).toIntOrNull() ?: 2026, pair)
    }

    fun calculateForPeriod(
        form: MemurFormState,
        kadro: MKadroFullRow?,
        year: Int,
        pair: SemesterColumnPair,
    ): SalaryResult {
        val period = "$year-${pair.ocakCoeffs.memurK}"
        val gvMeta = repo.gvMeta()
        val sem = SemesterCoeffs(ocak = pair.ocakCoeffs, temmuz = pair.temmuzCoeffs)
        val brackets = gvBrackets.brackets()
        val crossColumn = brackets.deductionMode == "crossColumn" ||
            repo.gvMeta().deductionMode == "crossColumn"

        val bl = blResolver.resolve(form, kadro, year.toString())
        val payOcak = HPayEngine.compute(bl, sem.ocak, form)
        val payTem = HPayEngine.compute(bl, sem.temmuz, form)

        val ded = deductionEngine.compute(
            payOcak = payOcak,
            payTem = payTem,
            sgkTipi = gvMeta.sgkTipi,
            primOran5434 = gvMeta.primOran5434,
            primOran5510 = gvMeta.primOran5510,
            dvMuafiyetIndex = repo.indexLookup("DV Muafiyet", period),
        )

        val monthlyNet = if (crossColumn && brackets.crossColumnThresholds != null) {
            CrossColumnNetCalculator.monthlyNet(
                payOcak = payOcak,
                payTem = payTem,
                primOcak = ded.primOcak,
                primTem = ded.primTem,
                gvMatrahOcak = ded.gvMatrahOcak,
                gvMatrahTem = ded.gvMatrahTem,
                brackets = brackets,
                thresholds = brackets.crossColumnThresholds!!,
            )
        } else {
            DoubleArray(12) { m ->
                val brut = if (m < 6) payOcak.brut132 else payTem.brut132
                val prim = if (m < 6) ded.primOcak else ded.primTem
                r2(brut - prim - ded.gvMonthly[m] - ded.dvMonthly[m])
            }
        }

        val aileBrut = familyBrutInPay(payTem.cells)
        val avgNet = r2(monthlyNet.average())
        val firstHalfAvg = r2(monthlyNet.take(6).average())
        val secondHalfAvg = r2(monthlyNet.drop(6).average())
        val yillikNet = r2(monthlyNet.sum())
        val yillikBrut = r2(payOcak.brut132 * 6 + payTem.brut132 * 6)
        val kesintiAylik = r2(
            monthlyNet.indices.map { i ->
                val prim = if (i < 6) ded.primOcak else ded.primTem
                prim + ded.gvMonthly[i] + ded.dvMonthly[i]
            }.average(),
        )
        val avgPrim = r2(
            monthlyNet.indices.map { i -> if (i < 6) ded.primOcak else ded.primTem }.average(),
        )
        val avgGv = r2(ded.gvMonthly.average())
        val avgDv = r2(ded.dvMonthly.average())
        val avgBrut = r2(
            monthlyNet.indices.map { i ->
                if (i < 6) payOcak.brut132 else payTem.brut132
            }.average(),
        )
        val deductionBreakdown = DeductionBreakdown(
            avgBrut = avgBrut,
            avgPrim = avgPrim,
            avgGv = avgGv,
            avgDv = avgDv,
            avgNet = avgNet,
        )

        val gold = repo.goldRate(period)
        val usd = repo.usdRate(period)
        val fx = fxRates.ratesForYear(year)?.let { rates ->
            FxOutputCalculator.compute(monthlyNet.toList(), rates)
        }

        return SalaryResult(
            brutAylik = r2(payTem.brut132),
            kesintiler = kesintiAylik,
            netAylik = avgNet,
            cocukYardimi = aileBrut,
            toplamNet = avgNet,
            yillikBrut = yillikBrut,
            yillikNet = yillikNet,
            firstHalfAvg = firstHalfAvg,
            secondHalfAvg = secondHalfAvg,
            altinGramKarsilik = if (gold > 0) r2(avgNet / gold * 10) / 10.0 else 0.0,
            dolarKarsilik = if (usd > 0) r2(avgNet / usd * 100) / 100.0 else 0.0,
            periodLabel = "$year (Excel H!${pair.ocak}/${pair.temmuz})",
            lineItems = buildLineItems(payOcak, payTem, ded, pair),
            monthlyNet = monthlyNet.toList(),
            fxOutput = fx,
            deductionBreakdown = deductionBreakdown,
        )
    }

    private fun familyBrutInPay(cells: Map<Int, Double>): Double = r2(
        (cells[124] ?: 0.0) + (cells[125] ?: 0.0) + (cells[126] ?: 0.0),
    )

    private fun buildLineItems(
        ocak: HPayEngine.PayResult,
        tem: HPayEngine.PayResult,
        ded: HDeductionEngine.DeductionResult,
        pair: SemesterColumnPair,
    ): List<String> {
        val fmt = { v: Double -> String.format("%,.2f ₺", v) }
        return listOf(
            "Ocak brüt (${pair.ocak}132): ${fmt(ocak.brut132)}",
            "Temmuz brüt (${pair.temmuz}132): ${fmt(tem.brut132)}",
            "GV matrah Ocak/Temmuz: ${fmt(ded.gvMatrahOcak)} / ${fmt(ded.gvMatrahTem)}",
            "Prim Ocak/Temmuz: ${fmt(ded.primOcak)} / ${fmt(ded.primTem)}",
        ) + tem.lines.map { (l, a) -> "$l: ${fmt(a)}" }
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
