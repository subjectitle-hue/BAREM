package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.GvBrackets
import kotlin.math.max
import kotlin.math.round

/** H!BM142–153 — kümülatif BL matrah üzerinden artan GV (Excel dilim tablosu). */
object GvCalculator {

    /** Kümülatif matrah (BL142…) üzerinden vergi — H!BM142 formülü. */
    fun cumulativeTax(bl: Double, b: GvBrackets): Double {
        val limits = b.dilimLimits
        val base = b.dilimBaseTax
        val rates = b.dilimRates
        return when {
            bl > limits[3] -> (bl - limits[3]) * rates[4] + base[3]
            bl > limits[2] -> (bl - limits[2]) * rates[3] + base[2]
            bl > limits[1] -> (bl - limits[1]) * rates[2] + base[1]
            bl > limits[0] -> (bl - limits[0]) * rates[1] + base[0]
            else -> bl * rates[0]
        }
    }

    /**
     * Aylık GV: (kümülatifVergi − önceki) − AGİ.
     * Ocak–Haz: bm140Ocak × ay; Temmuz+: bm140Ocak×6 + bm140Tem×(ay−6).
     */
    fun monthlyGv(bm140Ocak: Double, bm140Tem: Double, agi: Double, b: GvBrackets): DoubleArray {
        val gv = DoubleArray(12)
        var prevTax = 0.0
        for (m in 0 until 12) {
            val cumBl = if (m < 6) {
                bm140Ocak * (m + 1)
            } else {
                bm140Ocak * 6 + bm140Tem * (m - 5)
            }
            val tax = cumulativeTax(cumBl, b)
            gv[m] = r2(max(0.0, (tax - prevTax) - agi))
            prevTax = tax
        }
        return gv
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
