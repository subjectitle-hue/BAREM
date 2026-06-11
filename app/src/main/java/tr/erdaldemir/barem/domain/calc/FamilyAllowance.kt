package tr.erdaldemir.barem.domain.calc

import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.domain.model.MemurFormState
import kotlin.math.round

/** H!BN124–126 — aile yardımı (nete eklenir, brüte dahil değil). */
object FamilyAllowance {

    fun compute(form: MemurFormState, memurK: Double, repo: ExcelEngineRepository, period: String): Double {
        var total = 0.0
        if (form.medeniHal == "Evli" || form.medeniHal == "Evli-Eşi Çalışmayan") {
            val es = repo.indexLookup("Eş için Göstergesi (Aile Yardımı)", period)
            total += r2(es * memurK / 1000.0)
        }
        if (form.cocukUst6 > 0) {
            val c = repo.indexLookup("6 Yaşından Büyük Çocuk (Aile Yardımı)", period)
            total += r2(c * form.cocukUst6 * memurK / 1000.0)
        }
        if (form.cocukAlt6 > 0) {
            val c = repo.indexLookup("6 Yaşından Küçük Çocuk (Aile Yardımı)", period)
            total += r2(c * form.cocukAlt6 * memurK / 1000.0)
        }
        return total
    }

    private fun r2(v: Double): Double = round(v * 100.0) / 100.0
}
