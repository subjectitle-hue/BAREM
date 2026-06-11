package tr.erdaldemir.barem.domain.session

import android.content.Context
import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.FxRateRepository
import tr.erdaldemir.barem.data.engine.GvBracketRepository
import tr.erdaldemir.barem.data.engine.HFormulaRepository
import tr.erdaldemir.barem.data.engine.PeriodColumnResolver
import tr.erdaldemir.barem.data.engine.YearlyDataRepository
import tr.erdaldemir.barem.domain.PremiumAccess
import tr.erdaldemir.barem.domain.calc.ExcelMemurEngine
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine

/**
 * Premium açıldığında veya grafik ekranına girildiğinde yıllık seriyi yeniden hesaplar.
 */
object CalcSessionRefresher {

    fun needsYearlyRefresh(): Boolean {
        if (!PremiumAccess.isPremium()) return false
        if (!CalcSessionStore.hasActiveSession()) return false
        return CalcSessionStore.yearlySeries.isNullOrEmpty()
    }

    fun refreshYearlySeries(context: Context): Boolean {
        if (!needsYearlyRefresh()) return false
        val form = CalcSessionStore.lastForm ?: return false
        val appContext = context.applicationContext
        val engineRepo = ExcelEngineRepository(appContext)
        val hFormulas = HFormulaRepository(appContext)
        val gvBrackets = GvBracketRepository(appContext)
        val fxRates = FxRateRepository(appContext)
        val yearlyRepo = YearlyDataRepository(appContext)
        val periodResolver = PeriodColumnResolver(appContext)
        val excelEngine = ExcelMemurEngine(engineRepo, hFormulas, gvBrackets, fxRates)
        val yearlyEngine = YearlyCalcEngine(yearlyRepo, periodResolver, excelEngine, engineRepo, fxRates)
        val kadroFull = form.hizmetSinifi?.let { hs ->
            form.unvan?.let { u ->
                engineRepo.findKadroFull(hs, u, form.kadroDetay.orEmpty(), form.derece)
            }
        }
        val yearly = yearlyEngine.calculateSeries(form, kadroFull)
        CalcSessionStore.yearlySeries = yearly
        CalcSessionStore.notifyYearlyUpdated()
        return yearly.isNotEmpty()
    }
}
