package tr.erdaldemir.barem.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.HomeRoute
import tr.erdaldemir.barem.domain.model.showsBottomBar
import tr.erdaldemir.barem.domain.session.CalcSessionStore
import tr.erdaldemir.barem.domain.session.MemurRestoreTarget
import tr.erdaldemir.barem.ui.memur.MemurWizardScreen
import tr.erdaldemir.barem.ui.navigation.BaremBottomBar
import tr.erdaldemir.barem.ui.premium.PremiumSessionSync
import tr.erdaldemir.barem.ui.screens.AccountHubScreen
import tr.erdaldemir.barem.ui.screens.GeneralStatisticsHubScreen
import tr.erdaldemir.barem.ui.screens.GoldDollarScreen
import tr.erdaldemir.barem.ui.screens.GorusArticleScreen
import tr.erdaldemir.barem.ui.screens.GorusCategoryScreen
import tr.erdaldemir.barem.ui.screens.GorusHubScreen
import tr.erdaldemir.barem.ui.screens.HistoryScreen
import tr.erdaldemir.barem.ui.screens.PlaceholderWorkerScreen
import tr.erdaldemir.barem.ui.screens.SalaryChartsScreen
import tr.erdaldemir.barem.ui.screens.SalaryStatisticsTableScreen
import tr.erdaldemir.barem.ui.screens.SendikaPanelScreen
import tr.erdaldemir.barem.ui.screens.StatDetailScreen
import tr.erdaldemir.barem.ui.screens.VeriHubScreen
import tr.erdaldemir.barem.ui.screens.WelcomeScreen
import tr.erdaldemir.barem.ui.screens.YearlyAverageScreen

@Composable
fun BaremApp() {
    PremiumSessionSync()

    var navStackCsv by rememberSaveable { mutableStateOf("") }
    val stack = navStackCsv.split(",").filter { it.isNotEmpty() }
    val route = stack.lastOrNull()?.let { HomeRoute.fromSaved(it) }

    fun setStack(next: List<String>) {
        navStackCsv = next.joinToString(",")
    }

    fun push(next: HomeRoute) {
        setStack(stack + next.name)
    }

    fun pop() {
        if (stack.isEmpty()) return
        val leaving = stack.last()
        val nextStack = stack.dropLast(1)
        if (nextStack.lastOrNull() == HomeRoute.Memur.name && leaving != HomeRoute.Memur.name) {
            CalcSessionStore.requestMemurRestore(MemurRestoreTarget.RESULT)
        }
        setStack(nextStack)
    }

    fun goHome() {
        setStack(emptyList())
    }

    fun navigateToTool(tool: HomeRoute) {
        setStack(listOf(tool.name))
    }

    fun openMemurRestore(target: MemurRestoreTarget) {
        CalcSessionStore.requestMemurRestore(target)
        val memurIdx = stack.indexOf(HomeRoute.Memur.name)
        setStack(
            if (memurIdx >= 0) {
                stack.take(memurIdx + 1)
            } else {
                listOf(HomeRoute.Memur.name)
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route.showsBottomBar()) {
                BaremBottomBar(
                    currentRoute = route,
                    onNavigateHome = ::goHome,
                    onNavigateTool = ::navigateToTool,
                )
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            BaremNavHost(
                route = route,
                onNavigate = { next ->
                    if (next == null) goHome() else push(next)
                },
                onNavigateTool = ::navigateToTool,
                onPushRoute = ::push,
                onBack = { pop() },
                onMemurRestore = ::openMemurRestore,
                onGoHome = ::goHome,
            )
        }
    }
}

@Composable
private fun BaremNavHost(
    route: HomeRoute?,
    onNavigate: (HomeRoute?) -> Unit,
    onNavigateTool: (HomeRoute) -> Unit,
    onPushRoute: (HomeRoute) -> Unit,
    onBack: () -> Unit,
    onMemurRestore: (MemurRestoreTarget) -> Unit,
    onGoHome: () -> Unit,
) {
    when (route) {
        null -> WelcomeScreen(
            onNavigate = onNavigate,
            onOpenLastResult = { onMemurRestore(MemurRestoreTarget.RESULT) },
            onOpenLastCharts = {
                if (CalcSessionStore.hasActiveSession()) {
                    onNavigateTool(HomeRoute.SalaryCharts)
                }
            },
        )
        HomeRoute.Memur -> MemurWizardScreen(
            onExit = onGoHome,
            onOpenAnalytics = onNavigate,
        )
        HomeRoute.SozlesmeliPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.tile_sozlesmeli),
            onBack = onBack,
        )
        HomeRoute.KamuIscisiPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.tile_kamu_iscisi),
            onBack = onBack,
        )
        HomeRoute.EmekliPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.employee_emekli),
            onBack = onBack,
        )
        HomeRoute.AsgariUcretPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.tile_asgari_ucret),
            onBack = onBack,
        )
        HomeRoute.HarcirahPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.tile_harcirah),
            onBack = onBack,
        )
        HomeRoute.YurtdisiMaasPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.tile_yurtdisi_maas),
            onBack = onBack,
        )
        HomeRoute.DigerMaaslarPlaceholder -> PlaceholderWorkerScreen(
            title = stringResource(R.string.tile_diger_maaslar),
            onBack = onBack,
        )
        HomeRoute.AccountHub -> AccountHubScreen(
            onBack = onGoHome,
            onOpenRoute = onPushRoute,
        )
        HomeRoute.VeriHub -> VeriHubScreen(
            onBack = onGoHome,
            onOpenRoute = onPushRoute,
        )
        HomeRoute.GorusHub -> GorusHubScreen(
            onBack = onGoHome,
            onOpenCategory = onPushRoute,
        )
        HomeRoute.GorusCategory -> GorusCategoryScreen(
            onBack = onBack,
            onOpenTopic = onPushRoute,
        )
        HomeRoute.GorusArticle -> GorusArticleScreen(onBack = onBack)
        HomeRoute.SalaryCharts -> SalaryChartsScreen(
            onBack = onBack,
            onBackToResult = { onMemurRestore(MemurRestoreTarget.RESULT) },
            onEditSelection = { onMemurRestore(MemurRestoreTarget.EDIT_PERSONAL) },
        )
        HomeRoute.YearlyAverage -> YearlyAverageScreen(
            onBack = onBack,
            onBackToResult = { onMemurRestore(MemurRestoreTarget.RESULT) },
            onEditSelection = { onMemurRestore(MemurRestoreTarget.EDIT_PERSONAL) },
        )
        HomeRoute.GoldDollar -> GoldDollarScreen(
            onBack = onBack,
            onBackToResult = { onMemurRestore(MemurRestoreTarget.RESULT) },
            onEditSelection = { onMemurRestore(MemurRestoreTarget.EDIT_PERSONAL) },
        )
        HomeRoute.History -> HistoryScreen(
            onBack = onGoHome,
            onBackToResult = { onMemurRestore(MemurRestoreTarget.RESULT) },
            onEditSelection = { onMemurRestore(MemurRestoreTarget.EDIT_PERSONAL) },
            onRestoreHistory = { record ->
                CalcSessionStore.pendingHistoryRestore = record
                onMemurRestore(MemurRestoreTarget.RESULT)
            },
        )
        HomeRoute.Compare -> GeneralStatisticsHubScreen(
            onBack = onGoHome,
            onOpenRoute = onPushRoute,
        )
        HomeRoute.StatDetail -> StatDetailScreen(
            onBack = onBack,
            onOpenCharts = { onPushRoute(HomeRoute.SalaryCharts) },
        )
        HomeRoute.StatSalaryTable -> SalaryStatisticsTableScreen(
            onBack = onBack,
            onBackToResult = { onMemurRestore(MemurRestoreTarget.RESULT) },
            onEditSelection = { onMemurRestore(MemurRestoreTarget.EDIT_PERSONAL) },
            onOpenCharts = { onPushRoute(HomeRoute.SalaryCharts) },
        )
        HomeRoute.SendikaPanel -> SendikaPanelScreen(onBack = onBack)
    }
}
