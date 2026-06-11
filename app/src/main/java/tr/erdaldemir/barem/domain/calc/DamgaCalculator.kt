package tr.erdaldemir.barem.domain.calc

import kotlin.math.max
import kotlin.math.round

/** H!BN154–165 — damga vergisi (BL154 = BM141 × ay kümülatif). */
object DamgaCalculator {

    fun monthlyDamga(
        brutOcak: Double,
        brutTem: Double,
        rate: Double,
        muafBm: Double,
        muafBn: Double,
    ): DoubleArray {
        val dv = DoubleArray(12)
        var prevBm = 0.0
        for (m in 0 until 12) {
            val cumBrut = if (m < 6) {
                brutOcak * (m + 1)
            } else {
                brutOcak * 6 + brutTem * (m - 5)
            }
            val bm = r2(cumBrut * rate)
            val muaf = if (m < 6) muafBm else muafBn
            dv[m] = r2(max(0.0, (bm - prevBm) - muaf))
            prevBm = bm
        }
        return dv
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
