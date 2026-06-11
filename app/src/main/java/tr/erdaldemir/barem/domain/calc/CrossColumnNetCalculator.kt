package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.CrossColumnThresholds
import tr.erdaldemir.barem.data.engine.GvBrackets
import kotlin.math.max
import kotlin.math.round

/**
 * H!CO166–177 formül ailesi — Ocak sütunu brüt/prim + Temmuz sütunu GV/DV eşikleri.
 * 2023+ index dönemleri (CK/CL, CN/CO …) için Excel ile birebir net.
 */
object CrossColumnNetCalculator {

    fun monthlyNet(
        payOcak: HPayEngine.PayResult,
        payTem: HPayEngine.PayResult,
        primOcak: Double,
        primTem: Double,
        gvMatrahOcak: Double,
        gvMatrahTem: Double,
        brackets: GvBrackets,
        thresholds: CrossColumnThresholds,
    ): DoubleArray {
        val cnTax = DoubleArray(12)
        for (m in 0 until 6) {
            cnTax[m] = GvCalculator.cumulativeTax(gvMatrahOcak * (m + 1), brackets)
        }
        for (m in 0 until 6) {
            cnTax[6 + m] = GvCalculator.cumulativeTax(
                gvMatrahOcak * 6 + gvMatrahTem * (m + 1),
                brackets,
            )
        }

        val cnDvCum = cumulativeDamga(payOcak.brut132, payTem.brut132, brackets.damgaRate)
        val nets = DoubleArray(12)

        for (m in 0 until 6) {
            val gv = if (m == 0) {
                max(0.0, cnTax[0] - thresholds.gvFirstHalf[m])
            } else {
                max(0.0, cnTax[m] - cnTax[m - 1] - thresholds.gvFirstHalf[m])
            }
            val dv = if (m == 0) {
                max(0.0, cnDvCum[m] - thresholds.dvMuafOcak)
            } else {
                max(0.0, cnDvCum[m] - cnDvCum[m - 1] - thresholds.dvMuafOcak)
            }
            nets[m] = r2(payOcak.brut132 - primOcak - gv - dv)
        }

        for (m in 0 until 6) {
            val taxIdx = 6 + m
            val gv = if (m == 0) {
                max(0.0, cnTax[taxIdx] - cnTax[5] - thresholds.gvSecondHalf[m])
            } else {
                max(0.0, cnTax[taxIdx] - cnTax[taxIdx - 1] - thresholds.gvSecondHalf[m])
            }
            val dv = if (m == 0) {
                max(0.0, cnDvCum[6 + m] - cnDvCum[5 + m] - thresholds.dvMuafTem)
            } else {
                max(0.0, cnDvCum[6 + m] - cnDvCum[5 + m] - thresholds.dvMuafTem)
            }
            nets[6 + m] = r2(payTem.brut132 - primTem - gv - dv)
        }

        return nets
    }

    private fun cumulativeDamga(brutOcak: Double, brutTem: Double, rate: Double): DoubleArray {
        val cum = DoubleArray(12)
        for (m in 0 until 6) {
            cum[m] = r2(brutOcak * (m + 1) * rate)
        }
        for (m in 0 until 6) {
            cum[6 + m] = r2((brutOcak * 6 + brutTem * (m + 1)) * rate)
        }
        return cum
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
