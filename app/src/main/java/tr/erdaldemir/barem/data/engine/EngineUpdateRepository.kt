package tr.erdaldemir.barem.data.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.HttpURLConnection
import java.net.URL

/**
 * Faz 8 — Opsiyonel katsayı güncelleme kanalı (esmerimsi.com).
 * İlk kurulum assets; sync isteğe bağlı.
 */
class EngineUpdateRepository(context: Context) {

    private val gson = Gson()
    private val appContext = context.applicationContext

    val bundledManifest: EngineManifest by lazy {
        val json = appContext.assets.open("engine/manifest.json").bufferedReader().use { it.readText() }
        gson.fromJson(json, EngineManifest::class.java)
    }

    fun checkRemoteManifest(url: String = bundledManifest.updateUrl): EngineManifest? {
        return runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "GET"
            }
            conn.inputStream.bufferedReader().use { reader ->
                gson.fromJson(reader, EngineManifest::class.java)
            }
        }.getOrNull()
    }

    fun isUpdateAvailable(remote: EngineManifest?): Boolean {
        if (remote == null) return false
        return remote.engineVersion != bundledManifest.engineVersion
    }
}

data class EngineManifest(
    val version: Int,
    val engineVersion: String,
    val activeYear: Int,
    val ocakColumn: String,
    val temmuzColumn: String,
    val publishedAt: String?,
    val files: List<String>,
    @SerializedName("updateUrl") val updateUrl: String,
)
