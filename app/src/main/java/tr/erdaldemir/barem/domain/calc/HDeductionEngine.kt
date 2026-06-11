package tr.erdaldemir.barem.domain.calc



import tr.erdaldemir.barem.data.engine.GvBracketRepository

import tr.erdaldemir.barem.data.engine.GvBrackets

import kotlin.math.max

import kotlin.math.round



/**

 * H satır 133–165 — prim, GV matrah (CO140), damga.

 */

class HDeductionEngine(

    private val gvBrackets: GvBracketRepository,

) {



    data class DeductionResult(

        val primOcak: Double,

        val primTem: Double,

        val gvMonthly: DoubleArray,

        val dvMonthly: DoubleArray,

        val gvMatrahOcak: Double,

        val gvMatrahTem: Double,

    )



    fun compute(

        payOcak: HPayEngine.PayResult,

        payTem: HPayEngine.PayResult,

        sgkTipi: String,

        primOran5434: Double,

        primOran5510: Double,

        dvMuafiyetIndex: Double,

    ): DeductionResult {

        val b = gvBrackets.brackets()

        val primRate = if (sgkTipi == "5434") primOran5434 else primOran5510



        val primMatrahOcak = primMatrah(payOcak)

        val primMatrahTem = primMatrah(payTem)

        val primOcak = r2(primMatrahOcak * primRate / 100.0)

        val primTem = r2(primMatrahTem * primRate / 100.0)



        val gvMatrahOcak = computeGvMatrah(payOcak, primOcak)

        val gvMatrahTem = computeGvMatrah(payTem, primTem)



        val agi = b.agiMonthly

        val gv = GvCalculator.monthlyGv(gvMatrahOcak, gvMatrahTem, agi, b)

        val dv = DamgaCalculator.monthlyDamga(

            brutOcak = payOcak.brut132,

            brutTem = payTem.brut132,

            rate = b.damgaRate,

            muafBm = b.damgaMuafiyetBm ?: 0.0,

            muafBn = dvMuafiyetIndex,

        )



        return DeductionResult(

            primOcak = primOcak,

            primTem = primTem,

            gvMonthly = gv,

            dvMonthly = dv,

            gvMatrahOcak = gvMatrahOcak,

            gvMatrahTem = gvMatrahTem,

        )

    }



    /** H!CO133 = CO127+CO68+CO69+CO70+CO71 */

    private fun primMatrah(pay: HPayEngine.PayResult): Double = r2(

        pay.bn127 +

            (pay.cells[68] ?: 0.0) + (pay.cells[69] ?: 0.0) +

            (pay.cells[70] ?: 0.0) + (pay.cells[71] ?: 0.0),

    )



    /**

     * H!CO140 — GV matrahı (brüt dalına göre).

     * Varsayılan: CO68–71,78–80,97–103,118 − CO136 (prim).

     */

    private fun computeGvMatrah(pay: HPayEngine.PayResult, prim: Double): Double {

        val c = pay.cells

        val base = when {

            (c[113] ?: 0.0) > 0 -> (c[113] ?: 0.0) + (c[114] ?: 0.0)

            (c[111] ?: 0.0) > 0 -> c[111] ?: 0.0

            (c[128] ?: 0.0) > 0 -> c[128] ?: 0.0

            else -> defaultGvBase(c)

        }

        return r2(max(0.0, base - prim))

    }



    private fun defaultGvBase(c: Map<Int, Double>): Double {

        var sum = 0.0

        for (r in GV_BASE_ROWS) sum += c[r] ?: 0.0

        sum += c[118] ?: 0.0

        return sum

    }



    private fun r2(v: Double): Double = round(v * 100.0) / 100.0



    private companion object {

        val GV_BASE_ROWS = intArrayOf(68, 69, 70, 71, 78, 79, 80, 97, 98, 99, 100, 101, 102, 103)

    }

}


