package tr.erdaldemir.barem.ui.memur

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tr.erdaldemir.barem.data.CalcHistoryRepository
import tr.erdaldemir.barem.data.MemurCatalogRepository
import tr.erdaldemir.barem.data.VdLookupRepository
import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.FxRateRepository
import tr.erdaldemir.barem.data.engine.GvBracketRepository
import tr.erdaldemir.barem.data.engine.HFormulaRepository
import tr.erdaldemir.barem.data.engine.PeriodColumnResolver
import tr.erdaldemir.barem.data.engine.YearlyDataRepository
import tr.erdaldemir.barem.domain.calc.ExcelMemurEngine
import tr.erdaldemir.barem.BaremApplication
import tr.erdaldemir.barem.domain.PremiumAccess
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.model.HizmetSinifiLabels
import tr.erdaldemir.barem.domain.model.KademeRules
import tr.erdaldemir.barem.domain.model.MKadroRow
import tr.erdaldemir.barem.domain.model.MemurFormState
import tr.erdaldemir.barem.domain.model.TsMeslekRow
import tr.erdaldemir.barem.domain.session.CalcSessionRefresher
import tr.erdaldemir.barem.domain.session.CalcSessionStore
import tr.erdaldemir.barem.domain.session.MemurRestoreTarget

enum class MemurWizardStep(val index: Int, val title: String) {
    HIZMET_SINIFI(0, "Hizmet sınıfı"),
    KADRO(1, "Maaş unsurları"),
    KISISEL(2, "Kişisel bilgiler"),
    SONUC(3, "Sonuç"),
    ;

    companion object {
        val count = entries.size
    }
}

class MemurWizardViewModel(application: Application) : AndroidViewModel(application) {

    private val catalog = MemurCatalogRepository(application)
    private val historyRepo = CalcHistoryRepository(application)
    private val vdLookup = VdLookupRepository(application)
    private val engineRepo = ExcelEngineRepository(application)
    private val hFormulas = HFormulaRepository(application)
    private val gvBrackets = GvBracketRepository(application)
    private val yearlyRepo = YearlyDataRepository(application)
    private val fxRates = FxRateRepository(application)
    private val periodResolver = PeriodColumnResolver(application)
    private val excelEngine = ExcelMemurEngine(engineRepo, hFormulas, gvBrackets, fxRates)
    private val yearlyEngine = YearlyCalcEngine(yearlyRepo, periodResolver, excelEngine, engineRepo, fxRates)

    private val _state = MutableStateFlow(MemurWizardUiState())
    val state: StateFlow<MemurWizardUiState> = _state.asStateFlow()

    private val current get() = _state.value

    fun hizmetKolleri(): List<String> = catalog.hizmetKolleri()

    fun hizmetSiniflari(): List<String> = catalog.memurHizmetSiniflariOrdered()

    fun hizmetSinifiDisplay(code: String): String = HizmetSinifiLabels.display(code)

    fun meslekler(query: String = ""): List<TsMeslekRow> {
        val kolu = current.form.hizmetKolu ?: return emptyList()
        val sinifi = current.form.hizmetSinifi ?: return emptyList()
        return if (query.isBlank()) {
            catalog.meslekler(kolu, sinifi)
        } else {
            catalog.searchMeslekler(kolu, sinifi, query)
        }
    }

    fun kadroSecenekleri(): List<MKadroRow> =
        current.form.hizmetSinifi?.let { catalog.kadroSecenekleri(it) } ?: emptyList()

    fun unvanlar(): List<String> =
        current.form.hizmetSinifi?.let { catalog.unvanlar(it) } ?: emptyList()

    fun searchUnvanlar(query: String): List<String> {
        val sinifi = current.form.hizmetSinifi ?: return emptyList()
        return catalog.searchUnvanlar(sinifi, query)
    }

    fun detaylar(): List<String> {
        val sinifi = current.form.hizmetSinifi ?: return emptyList()
        val unvan = current.form.unvan ?: return emptyList()
        return catalog.detaylar(sinifi, unvan)
    }

    fun dereceler(): List<Int> {
        val sinifi = current.form.hizmetSinifi ?: return emptyList()
        val unvan = current.form.unvan ?: return emptyList()
        val detay = if (showDetayField()) current.form.kadroDetay else null
        return catalog.dereceler(sinifi, unvan, detay)
    }

    fun medeniHalSecenekleri(): List<String> = catalog.medeniHalSecenekleri()

    fun topluSozlesmeSecenekleri(): List<String> = listOf("Hayır", "Evet")

    fun topluSozlesmeLabel(stored: String): String =
        if (stored.equals("VAR", ignoreCase = true)) "Evet" else "Hayır"

    fun yabanciDilSecenekleri(): List<String> = catalog.yabanciDilSecenekleri()

    fun yabanciDilDisplay(stored: String?): String = stored ?: "Yok"

    fun iller(): List<String> = vdLookup.iller()

    fun ilceler(): List<String> =
        current.form.il?.let { vdLookup.ilceler(it) } ?: emptyList()

    fun kademeSecenekleri(): List<Int> {
        val derece = current.form.derece ?: return emptyList()
        return KademeRules.kademeler(derece)
    }

    /** 0–24 rakam; 25 = 25+ (motor max 25 yıl). */
    fun kidemSecenekleri(): List<Int> = (0..25).toList()

    fun kidemDisplayLabel(value: Int): String =
        if (value >= 25) "25+" else value.toString()

    fun hesapYillari(): List<Int> =
        periodResolver.availableYears(engineRepo.activeYear()).reversed()

    fun effectiveHesapYili(): Int = current.form.hesapYili ?: engineRepo.activeYear()

    fun showDetayField(): Boolean {
        val sinifi = current.form.hizmetSinifi ?: return false
        val unvan = current.form.unvan ?: return false
        return catalog.detaylar(sinifi, unvan).any { it.isNotBlank() }
    }

    fun cocukSayiSecenekleri(): List<Int> = (0..10).toList()

    fun contextSummaryLines(): List<String> {
        val f = current.form
        return buildList {
            f.hizmetKolu?.let { add("Hizmet kolu: $it") }
            f.hizmetSinifi?.let { add("Hizmet sınıfı: ${HizmetSinifiLabels.display(it)}") }
            f.hesapYili?.let { add("Hesap yılı: $it") }
            f.meslekLabel?.let { add("Meslek: $it") }
        }
    }

    fun selectHizmetKolu(value: String) {
        _state.update {
            it.copy(
                form = MemurFormState(hizmetKolu = value),
                salaryResult = null,
            )
        }
    }

    fun selectHizmetSinifi(value: String) {
        val kolu = catalog.resolveHizmetKolu(value)
        val meslek = kolu?.let { catalog.resolveMeslek(it, value, unvan = null) }
        _state.update {
            it.copy(
                form = it.form.copy(
                    hizmetKolu = kolu,
                    hizmetSinifi = value,
                    meslekKod = meslek?.kod,
                    meslekDetay = meslek?.detay,
                    hesapYili = null,
                    unvan = null,
                    kadroDetay = null,
                    derece = null,
                    kademe = null,
                    kidemYili = null,
                    il = null,
                    ilce = null,
                    bolgeselKod = null,
                ),
                salaryResult = null,
            )
        }
        if (current.step == MemurWizardStep.HIZMET_SINIFI) {
            goNext()
        }
    }

    fun selectHesapYili(value: Int) {
        _state.update { it.copy(form = it.form.copy(hesapYili = value), salaryResult = null) }
    }

    fun showIlIlceFields(): Boolean {
        val sinifi = current.form.hizmetSinifi ?: return false
        return sinifi in IL_ILCE_SINIFLERI
    }

    fun showCocukFields(): Boolean {
        val hal = current.form.medeniHal ?: return false
        return !hal.equals("Bekâr", ignoreCase = true)
    }

    fun selectKadro(row: MKadroRow) {
        selectUnvan(row.unvan)
        selectKadroDetay(row.detay)
        selectDerece(row.derece)
    }

    fun selectUnvan(value: String) {
        val sinifi = current.form.hizmetSinifi ?: return
        val detaylar = catalog.detaylar(sinifi, value)
        val autoDetay = when {
            detaylar.size == 1 -> detaylar.first()
            current.form.unvan == value -> current.form.kadroDetay
            else -> null
        }
        val autoDerece = autoDetay?.let { d ->
            val dereceler = catalog.dereceler(sinifi, value, d)
            when {
                dereceler.size == 1 -> dereceler.first()
                current.form.unvan == value && current.form.kadroDetay == d -> current.form.derece
                else -> null
            }
        }
        _state.update {
            it.copy(
                form = it.form.copy(
                    unvan = value,
                    kadroDetay = autoDetay,
                    derece = autoDerece,
                    kademe = null,
                ).let(::withSyncedMeslek),
                salaryResult = null,
            )
        }
    }

    fun selectKadroDetay(value: String) {
        val sinifi = current.form.hizmetSinifi ?: return
        val unvan = current.form.unvan ?: return
        val dereceler = catalog.dereceler(sinifi, unvan, value)
        val autoDerece = when {
            dereceler.size == 1 -> dereceler.first()
            current.form.kadroDetay == value -> current.form.derece
            else -> null
        }
        _state.update {
            it.copy(
                form = it.form.copy(
                    kadroDetay = value,
                    derece = autoDerece,
                    kademe = null,
                ),
                salaryResult = null,
            )
        }
    }

    fun selectDerece(value: Int?) {
        val validKademe = value?.let { KademeRules.kademeler(it) }.orEmpty()
        _state.update {
            val keptKademe = it.form.kademe?.takeIf { k -> k in validKademe }
            it.copy(
                form = it.form.copy(derece = value, kademe = keptKademe),
                salaryResult = null,
            )
        }
    }

    fun updateTopluSozlesme(label: String) {
        val stored = if (label.equals("Evet", ignoreCase = true)) "VAR" else "YOK"
        _state.update { it.copy(form = it.form.copy(topluSozlesme = stored), salaryResult = null) }
    }

    fun updateYabanciDil(label: String) {
        val stored = if (label.equals("Yok", ignoreCase = true)) null else label
        _state.update { it.copy(form = it.form.copy(yabanciDil = stored), salaryResult = null) }
    }

    fun selectIl(value: String) {
        val ilceler = vdLookup.ilceler(value)
        val autoIlce = when {
            ilceler.size == 1 -> ilceler.first()
            current.form.il == value -> current.form.ilce
            else -> null
        }
        val bolgesel = autoIlce?.let { vdLookup.bolgeselKod(value, it) }
        _state.update {
            it.copy(
                form = it.form.copy(
                    il = value,
                    ilce = autoIlce,
                    bolgeselKod = bolgesel,
                ),
                salaryResult = null,
            )
        }
    }

    fun selectIlce(value: String) {
        val il = current.form.il ?: return
        _state.update {
            it.copy(
                form = it.form.copy(
                    ilce = value,
                    bolgeselKod = vdLookup.bolgeselKod(il, value),
                ),
                salaryResult = null,
            )
        }
    }

    fun updateKademe(value: Int?) {
        _state.update { it.copy(form = it.form.copy(kademe = value), salaryResult = null) }
    }

    fun updateKidemYili(value: Int?) {
        _state.update { it.copy(form = it.form.copy(kidemYili = value), salaryResult = null) }
    }

    fun selectMedeniHal(value: String) {
        _state.update {
            val next = if (it.form.medeniHal == value) null else value
            val bekar = next?.equals("Bekâr", ignoreCase = true) == true
            it.copy(
                form = it.form.copy(
                    medeniHal = next,
                    cocukUst6 = if (bekar || next == null) 0 else it.form.cocukUst6,
                    cocukAlt6 = if (bekar || next == null) 0 else it.form.cocukAlt6,
                ),
                salaryResult = null,
            )
        }
    }

    fun updateCocukUst6(value: Int) {
        _state.update { it.copy(form = it.form.copy(cocukUst6 = value), salaryResult = null) }
    }

    fun updateCocukAlt6(value: Int) {
        _state.update { it.copy(form = it.form.copy(cocukAlt6 = value), salaryResult = null) }
    }

    fun canContinue(): Boolean = when (current.step) {
        MemurWizardStep.HIZMET_SINIFI -> current.form.hizmetSinifi != null
        MemurWizardStep.KADRO -> {
            current.form.unvan != null &&
                current.form.derece != null &&
                current.form.kademe != null &&
                current.form.kidemYili != null &&
                (!showDetayField() || current.form.kadroDetay != null)
        }
        MemurWizardStep.KISISEL -> true
        MemurWizardStep.SONUC -> true
    }

    fun goNext(): Boolean {
        if (!canContinue()) return false
        if (current.step == MemurWizardStep.SONUC) return false
        val nextStep = MemurWizardStep.entries[current.step.index + 1]
        if (nextStep == MemurWizardStep.SONUC) {
            computeAndShowResult()
            return true
        }
        _state.update { it.copy(step = nextStep) }
        return true
    }

    fun goBack(): Boolean {
        if (current.step == MemurWizardStep.HIZMET_SINIFI) return false
        _state.update {
            it.copy(
                step = MemurWizardStep.entries[current.step.index - 1],
                salaryResult = null,
            )
        }
        return true
    }

    fun reset() {
        _state.value = MemurWizardUiState()
        CalcSessionStore.clear()
    }

    fun refreshYearlySeriesIfNeeded() {
        if (!PremiumAccess.isPremium()) return
        if (current.yearlySeries.isNotEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                CalcSessionRefresher.refreshYearlySeries(getApplication())
            }
            _state.update {
                it.copy(yearlySeries = CalcSessionStore.yearlySeries.orEmpty())
            }
        }
    }

    fun consumeRestoreIntent() {
        CalcSessionStore.consumePendingHistory()?.let { record ->
            _state.update { it.copy(form = record.form) }
            computeAndShowResult()
            return
        }
        val target = CalcSessionStore.consumeMemurRestore() ?: return
        val form = CalcSessionStore.lastForm ?: return
        val step = when (target) {
            MemurRestoreTarget.RESULT -> MemurWizardStep.SONUC
            MemurRestoreTarget.EDIT_PERSONAL -> MemurWizardStep.KISISEL
        }
        _state.update {
            it.copy(
                step = step,
                form = form,
                salaryResult = CalcSessionStore.lastResult,
                yearlySeries = CalcSessionStore.yearlySeries.orEmpty(),
            )
        }
    }

    private fun computeAndShowResult() {
        val f = current.form
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                val kadroFull = f.hizmetSinifi?.let { hs ->
                    f.unvan?.let { u ->
                        engineRepo.findKadroFull(hs, u, f.kadroDetay.orEmpty(), f.derece)
                    }
                }
                val year = f.hesapYili ?: engineRepo.activeYear()
                val pair = engineRepo.semesterPairForYear(year) ?: engineRepo.activeSemesterPair()
                val salary = excelEngine.calculateForPeriod(f, kadroFull, year, pair)
                val yearly = if (PremiumAccess.isPremium()) {
                    yearlyEngine.calculateSeries(f, kadroFull)
                } else {
                    emptyList()
                }
                salary to yearly
            }
            _state.update {
                it.copy(
                    step = MemurWizardStep.SONUC,
                    salaryResult = result.first,
                    yearlySeries = result.second,
                )
            }
            CalcSessionStore.lastResult = result.first
            CalcSessionStore.lastForm = f
            CalcSessionStore.yearlySeries = result.second
            CalcSessionStore.notifyYearlyUpdated()
            historyRepo.saveFromSession(f, result.first)
        }
    }

    private fun withSyncedMeslek(form: MemurFormState): MemurFormState {
        val kolu = form.hizmetKolu ?: return form
        val sinifi = form.hizmetSinifi ?: return form
        val meslek = catalog.resolveMeslek(kolu, sinifi, form.unvan) ?: return form
        return form.copy(meslekKod = meslek.kod, meslekDetay = meslek.detay)
    }

    companion object {
        private val IL_ILCE_SINIFLERI = setOf("SHS", "THS", "ÜE")
    }
}
