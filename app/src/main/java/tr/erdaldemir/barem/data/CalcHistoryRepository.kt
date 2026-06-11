package tr.erdaldemir.barem.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tr.erdaldemir.barem.domain.model.MemurFormState
import tr.erdaldemir.barem.domain.model.SalaryResult
import tr.erdaldemir.barem.domain.session.CalcHistoryRecord
import tr.erdaldemir.barem.domain.session.CalcSessionStore
import java.io.File
import java.util.UUID

class CalcHistoryRepository(context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()
    private val file: File by lazy { File(appContext.filesDir, "calc_history.json") }

    fun list(): List<CalcHistoryRecord> = loadAll().sortedByDescending { it.savedAtEpochMs }

    fun find(id: String): CalcHistoryRecord? = loadAll().find { it.id == id }

    fun delete(id: String) {
        saveAll(loadAll().filterNot { it.id == id })
    }

    fun saveFromSession(form: MemurFormState, result: SalaryResult) {
        val record = CalcHistoryRecord(
            id = UUID.randomUUID().toString(),
            savedAtEpochMs = System.currentTimeMillis(),
            form = form,
            kadroSummary = CalcSessionStore.kadroSummaryLine(form).orEmpty(),
            netSummary = CalcSessionStore.netSummaryLine(result).orEmpty(),
            periodLabel = result.periodLabel,
        )
        val next = (listOf(record) + loadAll())
            .distinctBy { historyDedupeKey(it.form) }
            .take(MAX_ENTRIES)
        saveAll(next)
    }

    private fun historyDedupeKey(form: MemurFormState): String = buildString {
        append(form.hizmetKolu).append('|')
        append(form.hizmetSinifi).append('|')
        append(form.meslekKod).append('|')
        append(form.unvan).append('|')
        append(form.kadroDetay).append('|')
        append(form.hesapYili).append('|')
        append(form.derece).append('|')
        append(form.medeniHal).append('|')
        append(form.kademe).append('|')
        append(form.kidemYili).append('|')
        append(form.il).append('|')
        append(form.ilce).append('|')
        append(form.topluSozlesme).append('|')
        append(form.cocukUst6).append('|')
        append(form.cocukAlt6)
    }

    private fun loadAll(): List<CalcHistoryRecord> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CalcHistoryRecord>>() {}.type
            gson.fromJson<List<CalcHistoryRecord>>(file.readText(), type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun saveAll(records: List<CalcHistoryRecord>) {
        file.writeText(gson.toJson(records))
    }

    companion object {
        const val MAX_ENTRIES = 30
    }
}
