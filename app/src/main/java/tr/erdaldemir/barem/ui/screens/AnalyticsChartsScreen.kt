package tr.erdaldemir.barem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.domain.analytics.AnalyticsChartBuilder
import tr.erdaldemir.barem.domain.model.StatisticsNavStore
import tr.erdaldemir.barem.domain.session.CalcSessionStore
import tr.erdaldemir.barem.ui.premium.BaremPremiumUpsell
import tr.erdaldemir.barem.ui.premium.rememberEntitlement
import tr.erdaldemir.barem.ui.premium.rememberPremiumPurchaseAction
import tr.erdaldemir.barem.ui.premium.rememberPremiumRestoreAction
import tr.erdaldemir.barem.ui.premium.rememberSessionYearlyLoading
import tr.erdaldemir.barem.ui.premium.rememberSessionYearlySeries
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tr.erdaldemir.barem.ui.components.BaremCalcContextBar
import tr.erdaldemir.barem.ui.components.BaremDeductionStackChart
import tr.erdaldemir.barem.ui.components.BaremDualLineChart
import tr.erdaldemir.barem.ui.components.BaremMonthlyNetTable
import tr.erdaldemir.barem.ui.components.BaremYearLineChart
import tr.erdaldemir.barem.ui.theme.AccentGold
import tr.erdaldemir.barem.ui.theme.PrimaryBlue
import java.text.NumberFormat
import java.util.Locale

private enum class AnalyticsTab(val titleRes: Int) {
    TL(R.string.chart_tab_tl),
    USD(R.string.chart_tab_usd),
    EUR(R.string.chart_tab_eur),
    GOLD(R.string.chart_tab_gold),
    INFLATION(R.string.chart_tab_inflation),
    DEDUCTION(R.string.chart_tab_deduction),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsChartsScreen(
    onBack: () -> Unit,
    onBackToResult: () -> Unit = {},
    onEditSelection: () -> Unit = {},
) {
    val context = LocalContext.current
    val engineRepo = remember { ExcelEngineRepository(context.applicationContext) }
    val calc = CalcSessionStore.lastResult
    val yearly = rememberSessionYearlySeries()
    val yearlyLoading = rememberSessionYearlyLoading()
    val bundle = remember(calc, yearly) {
        AnalyticsChartBuilder.build(calc, yearly, engineRepo)
    }
    var selectedTab by remember {
        mutableIntStateOf(StatisticsNavStore.chartTabIndex.coerceIn(0, AnalyticsTab.entries.lastIndex))
    }
    val tabs = AnalyticsTab.entries
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    val onPurchase = rememberPremiumPurchaseAction()
    val onRestore = rememberPremiumRestoreAction()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chart_hub_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (bundle == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.calc_required_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = stringResource(tab.titleRes),
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (bundle != null) {
                    BaremCalcContextBar(
                        onBackToResult = onBackToResult,
                        onEditSelection = onEditSelection,
                    )
                }
                bundle.periodLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                when (tabs[selectedTab]) {
                    AnalyticsTab.TL -> {
                        Text(stringResource(R.string.chart_tl_subtitle), style = MaterialTheme.typography.bodyMedium)
                        BaremYearLineChart(
                            points = bundle.tlSeries,
                            valueFormatter = ::formatTry,
                            lineColor = PrimaryBlue,
                        )
                        calc?.takeIf { it.monthlyNet.isNotEmpty() }?.let { result ->
                            Text(
                                text = stringResource(R.string.chart_current_year_monthly),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            BaremMonthlyNetTable(
                                labels = result.monthlyLabels,
                                monthlyNet = result.monthlyNet,
                                formatAmount = ::formatTry,
                            )
                        }
                    }
                    AnalyticsTab.USD -> PremiumChartSection(
                        isPremium = isPremium,
                        loading = yearlyLoading,
                        onPurchase = onPurchase,
                        onRestore = onRestore,
                    ) {
                        Text(stringResource(R.string.chart_usd_subtitle), style = MaterialTheme.typography.bodyMedium)
                        BaremYearLineChart(
                            points = bundle.usdSeries,
                            valueFormatter = ::formatUsd,
                            lineColor = PrimaryBlue,
                        )
                    }
                    AnalyticsTab.EUR -> PremiumChartSection(
                        isPremium = isPremium,
                        loading = yearlyLoading,
                        onPurchase = onPurchase,
                        onRestore = onRestore,
                    ) {
                        Text(stringResource(R.string.chart_eur_subtitle), style = MaterialTheme.typography.bodyMedium)
                        BaremYearLineChart(
                            points = bundle.eurSeries,
                            valueFormatter = ::formatEur,
                            lineColor = PrimaryBlue,
                        )
                    }
                    AnalyticsTab.GOLD -> PremiumChartSection(
                        isPremium = isPremium,
                        loading = yearlyLoading,
                        onPurchase = onPurchase,
                        onRestore = onRestore,
                    ) {
                        Text(stringResource(R.string.chart_gold_subtitle), style = MaterialTheme.typography.bodyMedium)
                        BaremYearLineChart(
                            points = bundle.goldSeries,
                            valueFormatter = ::formatGoldQuarter,
                            lineColor = AccentGold,
                        )
                    }
                    AnalyticsTab.INFLATION -> PremiumChartSection(
                        isPremium = isPremium,
                        loading = yearlyLoading,
                        onPurchase = onPurchase,
                        onRestore = onRestore,
                    ) {
                        Text(stringResource(R.string.chart_inflation_subtitle), style = MaterialTheme.typography.bodyMedium)
                        BaremDualLineChart(
                            points = bundle.inflationVsSalary,
                            primaryLabel = stringResource(R.string.chart_inflation_line),
                            secondaryLabel = stringResource(R.string.chart_salary_growth_line),
                        )
                    }
                    AnalyticsTab.DEDUCTION -> PremiumChartSection(
                        isPremium = isPremium,
                        loading = yearlyLoading,
                        onPurchase = onPurchase,
                        onRestore = onRestore,
                    ) {
                        Text(stringResource(R.string.chart_deduction_subtitle), style = MaterialTheme.typography.bodyMedium)
                        BaremDeductionStackChart(points = bundle.deductionRates)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumChartSection(
    isPremium: Boolean,
    loading: Boolean,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    content: @Composable () -> Unit,
) {
    when {
        isPremium && loading -> {
            Text(
                text = stringResource(R.string.premium_data_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        isPremium -> content()
        else -> BaremPremiumUpsell(onPurchase = onPurchase, onRestore = onRestore, compact = true)
    }
}

private fun formatTry(value: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = 2
    fmt.maximumFractionDigits = 2
    return "${fmt.format(value)} ₺"
}

private fun formatUsd(value: Double): String = formatFx(value, "$")

private fun formatEur(value: Double): String = formatFx(value, "€")

private fun formatGoldQuarter(value: Double): String = formatFx(value, "çeyrek")

private fun formatFx(value: Double, suffix: String): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = 2
    fmt.maximumFractionDigits = 2
    return "${fmt.format(value)} $suffix"
}
