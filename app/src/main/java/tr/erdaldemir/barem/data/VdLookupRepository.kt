package tr.erdaldemir.barem.data

import android.content.Context
import com.google.gson.Gson
import tr.erdaldemir.barem.domain.model.VdIlIlceData

/** A!B13→B14→B16 — VD!AG:AH il/ilçe → bölgesel kod (export_vd_il_ilce.py). */
class VdLookupRepository(context: Context) {

    private val data: VdIlIlceData by lazy {
        val json = context.assets.open("memur/vd_il_ilce.json").bufferedReader().use { it.readText() }
        Gson().fromJson(json, VdIlIlceData::class.java)
    }

    fun iller(): List<String> = data.iller

    fun ilceler(il: String): List<String> = data.ilcelerByIl[il].orEmpty()

    /** Excel A!B16 — VD sütun AH; yoksa null (bölgesel tazminat uygulanmaz). */
    fun bolgeselKod(il: String, ilce: String): String? {
        val key = "${il.trim()}-${ilce.trim()}"
        return data.bolgeselKodByKey[key]
    }
}
