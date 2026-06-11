package tr.erdaldemir.barem.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.model.MemurFormState
import tr.erdaldemir.barem.domain.model.SalaryResult

/** Memur sihirbazına dönüş hedefi (UI adımı adıyla eşleşir). */
enum class MemurRestoreTarget {
    RESULT,
    EDIT_PERSONAL,
}

/** Son memur hesabı — grafik / yıllık ekranları besler. */
object CalcSessionStore {
    @Volatile
    var lastResult: SalaryResult? = null

    @Volatile
    var lastForm: MemurFormState? = null

    @Volatile
    var yearlySeries: List<YearlyCalcEngine.YearlyCalcRow>? = null

    private val _yearlyVersion = MutableStateFlow(0)
    val yearlyVersion: StateFlow<Int> = _yearlyVersion.asStateFlow()

    fun notifyYearlyUpdated() {
        _yearlyVersion.value++
    }

    /** Memur sihirbazına dönüşte açılacak hedef; tüketildikten sonra null yapılır. */
    @Volatile
    var memurRestoreTarget: MemurRestoreTarget? = null

    /** Geçmişten açılacak kayıt (History ekranı → Memur). */
    @Volatile
    var pendingHistoryRestore: CalcHistoryRecord? = null

    fun hasActiveSession(): Boolean =
        lastForm != null && lastResult != null

    fun clear() {
        lastResult = null
        lastForm = null
        yearlySeries = null
        memurRestoreTarget = null
        pendingHistoryRestore = null
    }

    fun requestMemurRestore(target: MemurRestoreTarget) {
        memurRestoreTarget = target
    }

    fun consumeMemurRestore(): MemurRestoreTarget? {
        val target = memurRestoreTarget
        memurRestoreTarget = null
        return target
    }

    fun consumePendingHistory(): CalcHistoryRecord? {
        val record = pendingHistoryRestore
        pendingHistoryRestore = null
        return record
    }

    /** Tek satır kadro özeti (grafik üst bandı / ana ekran kartı). */
    fun kadroSummaryLine(): String? = lastForm?.let { kadroSummaryLine(it) }

    fun kadroSummaryLine(form: MemurFormState): String? = buildString {
        form.unvan?.let { append(it) }
        form.derece?.let { d ->
            if (isNotEmpty()) append(" · ")
            append("$d. derece")
        }
        form.il?.let { il ->
            if (isNotEmpty()) append(" · ")
            append(il)
            form.ilce?.let { append("/$it") }
        }
    }.takeIf { it.isNotBlank() }

    fun netSummaryLine(): String? = lastResult?.let { netSummaryLine(it) }

    fun netSummaryLine(result: SalaryResult): String? = formatNet(result.netAylik)

    private fun formatNet(net: Double): String {
        val fmt = java.text.NumberFormat.getNumberInstance(java.util.Locale("tr", "TR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${fmt.format(net)} ₺ ort. net"
    }
}
