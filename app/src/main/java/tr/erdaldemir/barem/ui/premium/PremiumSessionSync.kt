package tr.erdaldemir.barem.ui.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.session.CalcSessionRefresher
import tr.erdaldemir.barem.domain.session.CalcSessionStore

/** Premium açılınca oturumdaki yıllık seriyi arka planda hesaplar. */
@Composable
fun PremiumSessionSync() {
    val context = LocalContext.current
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()

    LaunchedEffect(isPremium) {
        if (isPremium && CalcSessionRefresher.needsYearlyRefresh()) {
            withContext(Dispatchers.Default) {
                CalcSessionRefresher.refreshYearlySeries(context.applicationContext)
            }
            CalcSessionStore.notifyYearlyUpdated()
        }
    }
}

/**
 * Grafik / yıllık ekranları için oturum yıllık serisi.
 * Premium aktifken veri yoksa otomatik yeniler.
 */
@Composable
fun rememberSessionYearlySeries(): List<YearlyCalcEngine.YearlyCalcRow> {
    val context = LocalContext.current
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    val yearlyVersion by CalcSessionStore.yearlyVersion.collectAsStateWithLifecycle()
    var yearly by remember { mutableStateOf(CalcSessionStore.yearlySeries.orEmpty()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(isPremium, yearlyVersion) {
        if (!isPremium) {
            yearly = emptyList()
            loading = false
            return@LaunchedEffect
        }
        if (!CalcSessionStore.hasActiveSession()) {
            yearly = emptyList()
            return@LaunchedEffect
        }
        val cached = CalcSessionStore.yearlySeries
        if (!cached.isNullOrEmpty()) {
            yearly = cached
            loading = false
            return@LaunchedEffect
        }
        loading = true
        withContext(Dispatchers.Default) {
            CalcSessionRefresher.refreshYearlySeries(context.applicationContext)
        }
        yearly = CalcSessionStore.yearlySeries.orEmpty()
        loading = false
    }

    return yearly
}

@Composable
fun rememberSessionYearlyLoading(): Boolean {
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    val yearlyVersion by CalcSessionStore.yearlyVersion.collectAsStateWithLifecycle()
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isPremium, yearlyVersion) {
        if (!isPremium) {
            loading = false
            return@LaunchedEffect
        }
        loading = CalcSessionRefresher.needsYearlyRefresh()
        if (loading) {
            withContext(Dispatchers.Default) {
                CalcSessionRefresher.refreshYearlySeries(context.applicationContext)
            }
            CalcSessionStore.notifyYearlyUpdated()
            loading = false
        }
    }
    return loading
}
