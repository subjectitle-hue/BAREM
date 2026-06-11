package tr.erdaldemir.barem.domain.calc



import tr.erdaldemir.barem.data.engine.CoeffSet

import tr.erdaldemir.barem.data.engine.HPeriodColumn

import tr.erdaldemir.barem.domain.model.MemurFormState

import kotlin.math.round



/**

 * H satır 68–103, 105–109, 113–117, 128–131, 127 — ödeme kalemleri.

 * CO132 = IF(105>0 →105:109, IF(113>0 →113:117, IF(128>0 →128:131, SUM(68:103)))) + SUM(118:126)

 */

object HPayEngine {



    data class PayResult(

        val brut132: Double,

        val bn127: Double,

        val lines: List<Pair<String, Double>>,

        val cells: Map<Int, Double>,

    )



    fun compute(bl: Map<Int, Double>, coeff: CoeffSet, form: MemurFormState? = null): PayResult =

        compute(bl, coeff.toPeriodColumn(""), form)



    fun compute(bl: Map<Int, Double>, coeff: HPeriodColumn, form: MemurFormState? = null): PayResult {

        val memurK = coeff.memurK ?: 0.0

        val tabanK = coeff.tabanK ?: 0.0

        val yanK = coeff.yanK ?: 0.0

        val enY = coeff.enYuksek ?: 0.0



        val bn = mutableMapOf<Int, Double>()

        computeStandardRows(bl, bn, memurK, tabanK, yanK, enY)

        computeKistasBranch(bl, bn, memurK)

        computeEk10Branch(bl, bn, memurK)

        computeSpBranch(bl, bn, memurK, enY)



        val bl127 = bl[127] ?: 0.0

        bn[127] = if (bl127 > 0) r2(bl127 * enY / 100.0) else 0.0



        val baseBrut = selectBrutBase(bn)

        val brutExtras = brutRows118to126(bl, bn, memurK, form)

        val brut = r2(baseBrut + brutExtras)

        val lines = listOf(

            "Gösterge" to (bn[68] ?: 0.0),

            "Taban" to (bn[70] ?: 0.0),

            "Kıdem" to (bn[71] ?: 0.0),

            "Tazminat" to (bn[72] ?: 0.0),

            "Yan ödeme" to (bn[78] ?: 0.0),

            "Ek ödeme" to (bn[81] ?: 0.0),

        ).filter { it.second > 0 }



        return PayResult(brut, bn[127] ?: 0.0, lines, bn)

    }



    /** H!CO132 — aktif brüt dalı. */

    private fun selectBrutBase(bn: Map<Int, Double>): Double = when {

        (bn[105] ?: 0.0) > 0 -> r2((105..109).sumOf { bn[it] ?: 0.0 })

        (bn[113] ?: 0.0) > 0 -> r2((113..117).sumOf { bn[it] ?: 0.0 })

        (bn[128] ?: 0.0) > 0 -> r2((128..131).sumOf { bn[it] ?: 0.0 })

        else -> r2((68..103).sumOf { bn[it] ?: 0.0 })

    }



    private fun computeStandardRows(

        bl: Map<Int, Double>,

        bn: MutableMap<Int, Double>,

        memurK: Double,

        tabanK: Double,

        yanK: Double,

        enY: Double,

    ) {

        bn[68] = r2((bl[68] ?: 0.0) * memurK)

        bn[69] = r2((bl[69] ?: 0.0) * memurK)

        bn[70] = r2((bl[70] ?: 0.0) * tabanK)

        bn[71] = r2((bl[71] ?: 0.0) * memurK)

        bn[72] = r2((bl[72] ?: 0.0) * enY / 100.0)

        bn[73] = r2((bl[73] ?: 0.0) * enY / 100.0)

        bn[74] = r2((bl[74] ?: 0.0) * enY / 100.0)

        bn[75] = r2((bl[75] ?: 0.0) * enY / 100.0)

        bn[76] = r2((bl[76] ?: 0.0) * enY / 100.0)

        bn[77] = r2((bl[77] ?: 0.0) * enY / 100.0)

        bn[78] = r2((bl[78] ?: 0.0) * yanK)

        bn[79] = r2((bl[79] ?: 0.0) * yanK)

        bn[80] = r2((bl[80] ?: 0.0) * yanK)

        bn[81] = r2((bl[81] ?: 0.0) * enY / 100.0)

        for (r in 82..103) {

            val b = bl[r] ?: 0.0

            bn[r] = when {

                b <= 0 -> 0.0

                r == 92 || r == 96 -> r2((bn[68]!! + bn[69]!!) * b / 100.0)

                r in 101..103 -> r2(b * memurK / 100.0)

                else -> r2(b * enY / 100.0)

            }

        }

    }



    /** H!CO105–109 — kıstas aylık dalı (BL105>0). */

    private fun computeKistasBranch(bl: Map<Int, Double>, bn: MutableMap<Int, Double>, memurK: Double) {

        val cm105 = bl[105] ?: 0.0

        if (cm105 <= 0) return

        val cm110 = bl[110] ?: 0.0

        bn[110] = r2(cm110 * memurK)

        bn[105] = r2(cm105 / 100.0 * bn[110]!!)

        bn[106] = r2((bl[106] ?: 0.0) / 100.0 * bn[110]!!)

        bn[107] = r2((bl[107] ?: 0.0) / 100.0 * bn[105]!!)

        bn[108] = r2((bl[108] ?: 0.0) * memurK)

        bn[109] = r2((bl[109] ?: 0.0) * memurK)

    }



    /** H!CO113–117 — EK-10 ücret dalı (BL113>0). */

    private fun computeEk10Branch(bl: Map<Int, Double>, bn: MutableMap<Int, Double>, memurK: Double) {

        if ((bl[113] ?: 0.0) <= 0) return

        for (r in 113..117) {

            val cm = bl[r] ?: 0.0

            bn[r] = if (cm > 0) r2(cm * memurK) else 0.0

        }

    }



    /** H!CO128–131 — SP ücret dalı (BL128>0). CO128 ocak/temmuz geçişi Faz 3'te tamamlanır. */

    private fun computeSpBranch(

        bl: Map<Int, Double>,

        bn: MutableMap<Int, Double>,

        memurK: Double,

        enY: Double,

    ) {

        if ((bl[128] ?: 0.0) <= 0) return

        bn[128] = r2((bl[128] ?: 0.0) * (enY / 100.0 + 1.0))

        bn[129] = r2((bl[129] ?: 0.0) * enY / 100.0)

        bn[130] = r2((bl[130] ?: 0.0) * enY / 100.0)

        bn[131] = r2((bl[131] ?: 0.0) * enY / 100.0)

    }



    /** H!CO118–CO126 — brüte eklenen toplu ödeme kalemleri. */

    private fun brutRows118to126(

        bl: Map<Int, Double>,

        bn: MutableMap<Int, Double>,

        memurK: Double,

        form: MemurFormState?,

    ): Double {

        var extra = 0.0



        val r120 = bl[120] ?: 0.0

        if (r120 > 0) {

            val pay = r2(r120 * memurK)

            bn[120] = pay

            extra += pay

        }



        if (form?.topluSozlesme == "VAR") {

            val cm122 = bl[122] ?: CM122_DEFAULT

            val pay = r2(cm122 * memurK)

            bn[122] = pay

            extra += pay

        }



        if (form?.medeniHal == "Evli-Eşi Çalışmayan") {

            val cm124 = bl[124] ?: CM124_DEFAULT

            val pay = r2(cm124 * memurK)

            bn[124] = pay

            extra += pay

        }



        val cm125 = bl[125] ?: CM125_DEFAULT

        val ust6 = form?.cocukUst6 ?: 0

        if (ust6 > 0) {

            val pay = r2(cm125 * memurK * ust6)

            bn[125] = pay

            extra += pay

        }



        val cm126 = bl[126] ?: CM126_DEFAULT

        val alt6 = form?.cocukAlt6 ?: 0

        if (alt6 > 0) {

            val pay = r2(cm126 * memurK * alt6)

            bn[126] = pay

            extra += pay

        }



        return extra

    }



    private fun r2(v: Double): Double = round(v * 100.0) / 100.0



    private const val CM122_DEFAULT = 707.0

    private const val CM124_DEFAULT = 2273.0

    private const val CM125_DEFAULT = 250.0

    private const val CM126_DEFAULT = 500.0

}



private fun CoeffSet.toPeriodColumn(col: String) = HPeriodColumn(

    col = col,

    colIndex = 0,

    year = null,

    semester = null,

    memurK = memurK,

    tabanK = tabanK,

    yanK = yanK,

    enYuksek = enYuksek,

)


