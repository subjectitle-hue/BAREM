package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.MKadroFullRow
import tr.erdaldemir.barem.domain.model.MemurFormState
import kotlin.math.min

/**
 * H!CM68–CM127 — Excel BL/CM gösterge kaynakları (V, M, TS lookup).
 */
class BlResolver(
    private val repo: ExcelEngineRepository,
) {

    fun resolve(
        form: MemurFormState,
        kadro: MKadroFullRow?,
        year: String,
    ): Map<Int, Double> {
        val derece = form.derece ?: 1
        val kademe = form.kademe ?: 1
        val kidemYili = form.kidemYili ?: 0

        val mRow = repo.mLookupForKadro(kadro?.unvan, kadro?.detay, kadro?.derece)
        val ekCode = mRow?.ekCode ?: kadro?.ekGosterge

        val bl = mutableMapOf<Int, Double>()

        bl[68] = repo.vLookupDereceKademe(derece, kademe, year)
        bl[69] = repo.ekGostergePoints(ekCode, derece, year)
        bl[70] = kadro?.taban ?: 0.0
        bl[71] = kidemGosterge(kadro?.kidem ?: 0.0, kidemYili)
        bl[72] = repo.vLookupPayIndicator(mRow?.tazminatCode, derece, year)
        bl[73] = repo.vLookupPayIndicator(mRow?.ilaveTazPrimeDahilCode, derece, year)
        bl[74] = repo.vLookupPayIndicator(mRow?.ilaveTazPrimeHaricCode, derece, year)
        bl[75] = bolgeselTazminat(mRow?.bolgeselCode, form.bolgeselKod, year)
        bl[76] = repo.tsPrimeIndicatorForKadro(
            unvan = kadro?.unvan,
            detay = kadro?.detay,
            derece = kadro?.derece,
            year = year,
        )
        bl[77] = 0.0
        bl[78] = repo.vLookupPayIndicator(mRow?.yanOdemeCode, derece, year)
        bl[79] = 0.0
        bl[80] = 0.0
        bl[81] = repo.vLookupPayIndicator(mRow?.ekOdemeCode, derece, year)
        resolveExtendedRows(bl, mRow, derece, year)
        applyYabanciDil(bl, form, year)
        bl[122] = CM122
        bl[124] = CM124
        bl[125] = CM125
        bl[126] = CM126
        bl[127] = TyoTier.indicator(bl[69] ?: 0.0)

        val tazminatPath = (bl[72] ?: 0.0) > 0 ||
            (bl[91] ?: 0.0) > 0 ||
            (bl[105] ?: 0.0) > 0 ||
            (bl[113] ?: 0.0) > 0
        bl[120] = repo.ilaveOdemeGostergesi(year, tazminatPath)

        return bl
    }

    /** H!BL71 — min(500, kidemYili×M.kidem); kidemYili=0 iken 1 yıl sayılır. */
    private fun kidemGosterge(kidemBirim: Double, kidemYili: Int): Double {
        if (kidemBirim <= 0) return 0.0
        val yil = if (kidemYili <= 0) 1 else min(kidemYili, 25)
        return min(500.0, kidemBirim * yil)
    }

    /** H!BL75 — bölgesel tazminat (V lookup: kod-B16). */
    private fun bolgeselTazminat(code: String?, bolgeselKod: String?, year: String): Double {
        if (code.isNullOrBlank() || bolgeselKod.isNullOrBlank()) return 0.0
        return repo.vLookupTable("$code-$bolgeselKod", year)
    }

    /** H!BL82–103 — V/TS lookup iskeleti (M pay kodları export edildikçe dolar). */
    private fun resolveExtendedRows(
        bl: MutableMap<Int, Double>,
        mRow: tr.erdaldemir.barem.data.engine.MHLookupRow?,
        derece: Int,
        year: String,
    ) {
        for (r in 82..103) bl[r] = 0.0
        mRow?.payCodes?.forEach { (row, code) ->
            val rowNum = row.toIntOrNull() ?: return@forEach
            if (rowNum !in 82..103) return@forEach
            bl[rowNum] = repo.vLookupPayIndicator(code, derece, year)
        }
    }

    /** A!B11 — yabancı dil tazminatı → H!BL79 (V: A1 / B / C). */
    private fun applyYabanciDil(
        bl: MutableMap<Int, Double>,
        form: MemurFormState,
        year: String,
    ) {
        val level = form.yabanciDil ?: return
        val vKey = when (level.uppercase()) {
            "A", "A1" -> "A1"
            "A2" -> "A2"
            "B" -> "B"
            "C" -> "C"
            else -> return
        }
        val points = repo.vLookupTable(vKey, year)
        if (points > 0) bl[79] = points
    }

    private companion object {
        const val CM122 = 707.0
        const val CM124 = 2273.0
        const val CM125 = 250.0
        const val CM126 = 500.0
    }
}
