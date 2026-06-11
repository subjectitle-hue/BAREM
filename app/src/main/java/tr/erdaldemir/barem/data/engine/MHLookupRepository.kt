package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** M sayfası → H BL satır 69/76 lookup (export_m_h_lookup.py). */
class MHLookupRepository(context: Context) {

    private val byKey: Map<String, MHLookupRow> by lazy {
        val json = context.assets.open("engine/m_h_lookup.json").bufferedReader().use { it.readText() }
        val root = Gson().fromJson(json, MHLookupRoot::class.java)
        root.rows.associateBy { it.key }
    }

    fun find(unvan: String, detay: String, derece: Int): MHLookupRow? {
        val key = "${unvan.trim()}-${detay.trim()}-$derece"
        return byKey[key]
    }
}

data class MHLookupRoot(val rows: List<MHLookupRow>)

data class MHLookupRow(
    val key: String,
    val hizmetSinifi: String,
    val unvan: String,
    val detay: String,
    val derece: Int,
    val ekCode: String?,
    val tazminatCode: String? = null,
    val ilaveTazPrimeDahilCode: String? = null,
    val ilaveTazPrimeHaricCode: String? = null,
    val yanOdemeCode: String? = null,
    val ekOdemeCode: String? = null,
    val bolgeselCode: String? = null,
    val payCodes: Map<String, String>? = null,
    val tsPrimeKod: String?,
)
