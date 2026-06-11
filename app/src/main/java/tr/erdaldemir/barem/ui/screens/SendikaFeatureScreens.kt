package tr.erdaldemir.barem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tr.erdaldemir.barem.domain.session.CalcSessionRefresher
import tr.erdaldemir.barem.data.CalcHistoryRepository
import tr.erdaldemir.barem.domain.session.CalcHistoryRecord
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.YearlyDataRepository
import tr.erdaldemir.barem.domain.model.SalaryResult
import tr.erdaldemir.barem.domain.model.StatisticsNavStore
import tr.erdaldemir.barem.ui.premium.BaremPremiumGate
import tr.erdaldemir.barem.ui.premium.BaremPremiumUpsell
import tr.erdaldemir.barem.ui.premium.rememberEntitlement
import tr.erdaldemir.barem.ui.premium.rememberPremiumPurchaseAction
import tr.erdaldemir.barem.ui.premium.rememberPremiumRestoreAction
import tr.erdaldemir.barem.ui.premium.rememberSessionYearlyLoading
import tr.erdaldemir.barem.ui.premium.rememberSessionYearlySeries
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tr.erdaldemir.barem.BuildConfig
import tr.erdaldemir.barem.data.billing.BillingEntitlementRepository
import tr.erdaldemir.barem.domain.calc.YearlyCalcEngine
import tr.erdaldemir.barem.domain.session.CalcSessionStore
import tr.erdaldemir.barem.ui.components.BaremAmountLine
import tr.erdaldemir.barem.ui.components.BaremCalcContextBar
import tr.erdaldemir.barem.ui.components.BaremMonthlyNetTable
import tr.erdaldemir.barem.ui.components.BaremStatPairRow
import tr.erdaldemir.barem.ui.theme.AccentGold
import tr.erdaldemir.barem.ui.theme.PrimaryBlue
import tr.erdaldemir.barem.ui.theme.SurfaceElevated
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendikaFeatureScaffold(
    title: String,
    onBack: () -> Unit,
    onBackToResult: (() -> Unit)? = null,
    onEditSelection: (() -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val columnModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .then(
                if (scrollable) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                },
            )
        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (onBackToResult != null && onEditSelection != null) {
                BaremCalcContextBar(
                    onBackToResult = onBackToResult,
                    onEditSelection = onEditSelection,
                )
            }
            content()
        }
    }
}

@Composable
private fun CalcRequiredNotice() {
    Text(
        text = stringResource(R.string.calc_required_message),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun SalaryChartsScreen(
    onBack: () -> Unit,
    onBackToResult: () -> Unit = {},
    onEditSelection: () -> Unit = {},
) {
    AnalyticsChartsScreen(
        onBack = onBack,
        onBackToResult = onBackToResult,
        onEditSelection = onEditSelection,
    )
}

@Composable
fun YearlyAverageScreen(
    onBack: () -> Unit,
    onBackToResult: () -> Unit = {},
    onEditSelection: () -> Unit = {},
) {
    val context = LocalContext.current
    val yearlyRepo = remember { YearlyDataRepository(context.applicationContext) }
    val calc = CalcSessionStore.lastResult
    val yearly = rememberSessionYearlySeries()
    val yearlyLoading = rememberSessionYearlyLoading()
    val activeYear = yearlyRepo.activeYear()
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    val onPurchase = rememberPremiumPurchaseAction()
    val onRestore = rememberPremiumRestoreAction()

    SendikaFeatureScaffold(
        title = stringResource(R.string.yearly_bordro_title),
        onBack = onBack,
        onBackToResult = onBackToResult,
        onEditSelection = onEditSelection,
    ) {
        if (calc == null) {
            CalcRequiredNotice()
            return@SendikaFeatureScaffold
        }

        val currentRow = yearly?.find { it.year == activeYear }
        val gold = currentRow?.goldPerGram ?: yearlyRepo.goldRate(activeYear) ?: 0.0
        val usd = currentRow?.usdRate ?: yearlyRepo.usdRate(activeYear) ?: 0.0

        Text(
            text = calc.periodLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BaremAmountLine(stringResource(R.string.yearly_avg_brut), formatTry(calc.yillikBrut))
        BaremAmountLine(stringResource(R.string.yearly_avg_net), formatTry(calc.yillikNet), highlight = true)
        BaremAmountLine("Aylık ort. net", formatTry(calc.netAylik))
        if (gold > 0) {
            BaremAmountLine(
                stringResource(R.string.yearly_avg_gold),
                "${formatNum(calc.netAylik / gold)} gr altın / ay ort.",
            )
        }
        if (usd > 0) {
            BaremAmountLine(
                stringResource(R.string.yearly_avg_usd),
                "${formatNum(calc.netAylik / usd)} $ / ay ort.",
            )
        }

        when {
            isPremium && yearlyLoading -> {
                Text(
                    text = stringResource(R.string.premium_data_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            isPremium && yearly.isNotEmpty() -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(R.string.yearly_history_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                yearly.sortedByDescending { it.year }.forEach { row ->
                    YearlyHistoryRow(row)
                }
            }
            !isPremium -> {
                BaremPremiumUpsell(
                    onPurchase = onPurchase,
                    onRestore = onRestore,
                    compact = true,
                )
            }
        }
    }
}

@Composable
private fun YearlyHistoryRow(row: YearlyCalcEngine.YearlyCalcRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.year.toString(),
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTry(row.yillikNet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "ort. ${formatTry(row.firstHalfAvg)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun GoldDollarScreen(
    onBack: () -> Unit,
    onBackToResult: () -> Unit = {},
    onEditSelection: () -> Unit = {},
) {
    val calc = CalcSessionStore.lastResult
    val yearly = rememberSessionYearlySeries()
    val yearlyLoading = rememberSessionYearlyLoading()
    val fx = calc?.fxOutput
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    val onPurchase = rememberPremiumPurchaseAction()
    val onRestore = rememberPremiumRestoreAction()

    SendikaFeatureScaffold(
        title = stringResource(R.string.tile_fx),
        onBack = onBack,
        onBackToResult = onBackToResult,
        onEditSelection = onEditSelection,
    ) {
        if (calc == null || fx == null) {
            CalcRequiredNotice()
            return@SendikaFeatureScaffold
        }
        Text(
            text = calc.periodLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FxBlock(
            title = stringResource(R.string.fx_usd_table_title),
            labels = calc.monthlyLabels,
            monthly = fx.monthlyUsd,
            formatAmount = ::formatUsd,
            valueColumnLabel = stringResource(R.string.fx_col_usd),
            firstHalf = fx.firstHalfUsd,
            secondHalf = fx.secondHalfUsd,
            yearlyAvg = fx.yearlyAvgUsd,
        )
        FxBlock(
            title = stringResource(R.string.fx_eur_table_title),
            labels = calc.monthlyLabels,
            monthly = fx.monthlyEur,
            formatAmount = ::formatEur,
            valueColumnLabel = stringResource(R.string.fx_col_eur),
            firstHalf = fx.firstHalfEur,
            secondHalf = fx.secondHalfEur,
            yearlyAvg = fx.yearlyAvgEur,
        )
        FxBlock(
            title = stringResource(R.string.fx_gold_quarter_table_title),
            labels = calc.monthlyLabels,
            monthly = fx.monthlyGoldQuarter,
            formatAmount = ::formatGoldQuarter,
            valueColumnLabel = stringResource(R.string.fx_col_gold_quarter),
            firstHalf = fx.firstHalfGoldQuarter,
            secondHalf = fx.secondHalfGoldQuarter,
            yearlyAvg = fx.yearlyAvgGoldQuarter,
        )

        when {
            isPremium && yearlyLoading -> {
                Text(
                    text = stringResource(R.string.premium_data_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            isPremium && yearly.isNotEmpty() -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(R.string.fx_yearly_history_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                yearly.sortedByDescending { it.year }.forEach { row ->
                    FxYearlyHistoryRow(row)
                }
            }
            !isPremium -> {
                BaremPremiumUpsell(
                    onPurchase = onPurchase,
                    onRestore = onRestore,
                    compact = true,
                )
            }
        }
    }
}

@Composable
private fun FxBlock(
    title: String,
    labels: List<String>,
    monthly: List<Double>,
    formatAmount: (Double) -> String,
    valueColumnLabel: String,
    firstHalf: Double,
    secondHalf: Double,
    yearlyAvg: Double,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
    BaremMonthlyNetTable(
        labels = labels,
        monthlyNet = monthly,
        formatAmount = formatAmount,
        valueColumnLabel = valueColumnLabel,
        modifier = Modifier.padding(top = 6.dp),
    )
    BaremStatPairRow(
        label = stringResource(R.string.result_first_half_avg),
        value = formatAmount(firstHalf),
        modifier = Modifier.padding(top = 6.dp),
    )
    BaremStatPairRow(
        label = stringResource(R.string.result_second_half_avg),
        value = formatAmount(secondHalf),
    )
    BaremStatPairRow(
        label = stringResource(R.string.result_yearly_avg_net),
        value = formatAmount(yearlyAvg),
        highlight = true,
    )
}

@Composable
private fun FxYearlyHistoryRow(row: YearlyCalcEngine.YearlyCalcRow) {
    val fx = row.fxOutput
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = row.year.toString(), style = MaterialTheme.typography.bodyMedium)
        Column(horizontalAlignment = Alignment.End) {
            if (fx != null) {
                Text(
                    text = formatUsd(fx.yearlyAvgUsd),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${formatEur(fx.yearlyAvgEur)} · ${formatGoldQuarter(fx.yearlyAvgGoldQuarter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = formatTry(row.yillikNet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onBackToResult: () -> Unit = {},
    onEditSelection: () -> Unit = {},
    onRestoreHistory: (CalcHistoryRecord) -> Unit = {},
) {
    val context = LocalContext.current
    val historyRepo = remember { CalcHistoryRepository(context.applicationContext) }
    var records by remember { mutableStateOf(historyRepo.list()) }
    val refresh: () -> Unit = { records = historyRepo.list() }

    SendikaFeatureScaffold(
        title = stringResource(R.string.tile_gecmis),
        onBack = onBack,
        onBackToResult = onBackToResult,
        onEditSelection = onEditSelection,
    ) {
        if (records.isEmpty() && !CalcSessionStore.hasActiveSession()) {
            CalcRequiredNotice()
            return@SendikaFeatureScaffold
        }
        Text(
            text = stringResource(R.string.history_list_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (CalcSessionStore.hasActiveSession()) {
            Text(
                text = stringResource(R.string.history_current_session),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            HistorySessionCard(
                title = CalcSessionStore.kadroSummaryLine().orEmpty(),
                subtitle = buildString {
                    CalcSessionStore.netSummaryLine()?.let { append(it) }
                    CalcSessionStore.lastResult?.periodLabel?.let {
                        if (isNotEmpty()) append(" · ")
                        append(it)
                    }
                },
                onOpen = onBackToResult,
            )
        }
        records.forEach { record ->
            HistorySessionCard(
                title = record.kadroSummary.ifBlank { record.periodLabel },
                subtitle = "${record.netSummary} · ${formatHistoryDate(record.savedAtEpochMs)}",
                onOpen = {
                    onRestoreHistory(record)
                    refresh()
                },
                onDelete = {
                    historyRepo.delete(record.id)
                    refresh()
                },
            )
        }
    }
}

@Composable
private fun HistorySessionCard(
    title: String,
    subtitle: String,
    onOpen: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.history_delete))
                }
            }
            TextButton(onClick = onOpen) {
                Text(stringResource(R.string.history_open))
            }
        }
    }
}

private fun formatHistoryDate(epochMs: Long): String {
    val fmt = java.text.DateFormat.getDateTimeInstance(
        java.text.DateFormat.SHORT,
        java.text.DateFormat.SHORT,
        java.util.Locale("tr", "TR"),
    )
    return fmt.format(java.util.Date(epochMs))
}

private const val STATISTICS_YEAR_START = 2000
private const val STATISTICS_YEAR_END = 2026

private data class StatisticsValues(
    val tl: Double,
    val usd: Double?,
    val eur: Double?,
    val goldQuarter: Double?,
    val asgariRatio: Double?,
)

private enum class SalaryStatTab(val titleRes: Int) {
    TL(R.string.chart_tab_tl),
    USD(R.string.chart_tab_usd),
    EUR(R.string.chart_tab_eur),
    GOLD(R.string.chart_tab_gold),
    ASGARI(R.string.statistics_col_asgari),
}

@Composable
fun SalaryStatisticsTableScreen(
    onBack: () -> Unit,
    onBackToResult: () -> Unit = {},
    onEditSelection: () -> Unit = {},
    onOpenCharts: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val excelRepo = remember { ExcelEngineRepository(context.applicationContext) }
    val computed = rememberSessionYearlySeries()
    val yearlyLoading = rememberSessionYearlyLoading()
    val onPurchase = rememberPremiumPurchaseAction()
    val onRestore = rememberPremiumRestoreAction()
    var expandedYear by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val statTabs = SalaryStatTab.entries

    LaunchedEffect(Unit) {
        if (CalcSessionStore.hasActiveSession() && CalcSessionRefresher.needsYearlyRefresh()) {
            withContext(Dispatchers.Default) {
                CalcSessionRefresher.refreshYearlySeries(context.applicationContext)
            }
        }
    }

    SendikaFeatureScaffold(
        title = stringResource(R.string.result_hub_salary_stats),
        onBack = onBack,
        onBackToResult = onBackToResult,
        onEditSelection = onEditSelection,
    ) {
        BaremPremiumGate(
            onPurchase = onPurchase,
            onRestore = onRestore,
            previewContent = {
                CalcSessionStore.lastResult?.let { result ->
                    BaremAmountLine(
                        "${result.periodLabel} net (güncel)",
                        formatTry(result.netAylik),
                        highlight = true,
                    )
                }
            },
        ) {
            if (CalcSessionStore.lastResult == null) {
                CalcRequiredNotice()
                return@BaremPremiumGate
            }
            if (yearlyLoading) {
                Text(
                    text = stringResource(R.string.premium_data_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@BaremPremiumGate
            }
            if (computed.isEmpty()) {
                CalcRequiredNotice()
                return@BaremPremiumGate
            }

            if (onOpenCharts != null) {
                Button(
                    onClick = {
                        StatisticsNavStore.chartTabIndex = 0
                        onOpenCharts()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.stat_show_chart))
                }
            }

            Text(
                text = stringResource(R.string.statistics_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TabRow(selectedTabIndex = selectedTab) {
                statTabs.forEachIndexed { index, tab ->
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

            val activeTab = statTabs[selectedTab]
            val filtered = computed
                .filter { it.year in STATISTICS_YEAR_START..STATISTICS_YEAR_END }
                .sortedByDescending { it.year }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                StatisticsTableHeader(activeTab)
                filtered.forEach { row ->
                    val yearlyValues = statisticsYearlyValues(row, excelRepo)
                    val isExpanded = expandedYear == row.year
                    StatisticsDataRow(
                        periodLabel = row.year.toString(),
                        valueText = formatStatisticsValue(yearlyValues, activeTab),
                        emphasized = true,
                        expanded = isExpanded,
                        onClick = {
                            expandedYear = if (isExpanded) null else row.year
                        },
                    )
                    if (isExpanded) {
                        val fx = row.fxOutput
                        val monthLabels = SalaryResult.MONTH_LABELS
                        monthLabels.forEachIndexed { index, month ->
                            val net = row.monthlyNet.getOrNull(index) ?: return@forEachIndexed
                            val monthlyValues = StatisticsValues(
                                tl = net,
                                usd = fx?.monthlyUsd?.getOrNull(index),
                                eur = fx?.monthlyEur?.getOrNull(index),
                                goldQuarter = fx?.monthlyGoldQuarter?.getOrNull(index),
                                asgariRatio = asgariRatio(net, row.year, index, excelRepo),
                            )
                            StatisticsDataRow(
                                periodLabel = month,
                                valueText = formatStatisticsValue(monthlyValues, activeTab),
                                emphasized = false,
                                expanded = false,
                                onClick = null,
                                indent = true,
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.compare_source_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun StatisticsTableHeader(tab: SalaryStatTab) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatisticsCell(
            text = stringResource(R.string.statistics_col_period),
            style = MaterialTheme.typography.labelMedium,
            width = StatisticsPeriodWidth,
            bold = true,
        )
        StatisticsCell(
            text = stringResource(tab.titleRes),
            bold = true,
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider()
}

@Composable
private fun StatisticsDataRow(
    periodLabel: String,
    valueText: String,
    emphasized: Boolean,
    expanded: Boolean,
    onClick: (() -> Unit)?,
    indent: Boolean = false,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(vertical = 6.dp, horizontal = 4.dp)
        .then(if (indent) Modifier.padding(start = 12.dp) else Modifier)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.width(StatisticsPeriodWidth),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onClick != null) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 2.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (indent) {
                Box(modifier = Modifier.width(22.dp))
            }
            Text(
                text = periodLabel,
                style = if (emphasized) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodySmall
                },
                color = if (emphasized) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        StatisticsCell(
            text = valueText,
            emphasized = emphasized,
            modifier = Modifier.weight(1f),
        )
    }
    if (emphasized) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    }
}

@Composable
private fun StatisticsCell(
    text: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall,
    width: androidx.compose.ui.unit.Dp? = StatisticsPeriodWidth,
    bold: Boolean = false,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = if (bold || emphasized) MaterialTheme.typography.bodyMedium else style,
        color = when {
            bold -> MaterialTheme.colorScheme.onSurface
            emphasized -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .padding(horizontal = 4.dp),
        maxLines = 1,
    )
}

private val StatisticsPeriodWidth = 88.dp

private fun formatStatisticsValue(values: StatisticsValues, tab: SalaryStatTab): String = when (tab) {
    SalaryStatTab.TL -> formatTry(values.tl)
    SalaryStatTab.USD -> formatUsdOrDash(values.usd)
    SalaryStatTab.EUR -> formatEurOrDash(values.eur)
    SalaryStatTab.GOLD -> formatGoldOrDash(values.goldQuarter)
    SalaryStatTab.ASGARI -> formatAsgariRatio(values.asgariRatio)
}

private fun statisticsYearlyValues(
    row: YearlyCalcEngine.YearlyCalcRow,
    excelRepo: ExcelEngineRepository,
): StatisticsValues {
    val tl = row.monthlyNet.takeIf { it.isNotEmpty() }?.average()
        ?: (row.firstHalfAvg + row.secondHalfAvg) / 2.0
    val fx = row.fxOutput
    val ratios = row.monthlyNet.mapIndexedNotNull { index, net ->
        asgariRatio(net, row.year, index, excelRepo)
    }
    return StatisticsValues(
        tl = tl,
        usd = fx?.yearlyAvgUsd,
        eur = fx?.yearlyAvgEur,
        goldQuarter = fx?.yearlyAvgGoldQuarter,
        asgariRatio = ratios.takeIf { it.isNotEmpty() }?.average(),
    )
}

private fun asgariRatio(
    net: Double,
    year: Int,
    monthIndex: Int,
    excelRepo: ExcelEngineRepository,
): Double? {
    val asgari = excelRepo.netAsgariUcret(year, secondHalf = monthIndex >= 6) ?: return null
    return if (asgari > 0) net / asgari else null
}

private fun formatUsdOrDash(value: Double?): String =
    value?.let(::formatUsd) ?: "—"

private fun formatEurOrDash(value: Double?): String =
    value?.let(::formatEur) ?: "—"

private fun formatGoldOrDash(value: Double?): String =
    value?.let(::formatGoldQuarter) ?: "—"

private fun formatAsgariRatio(ratio: Double?): String =
    ratio?.let { "${formatNum(it)}×" } ?: "—"

@Composable
fun SendikaPanelScreen(onBack: () -> Unit) {
    SendikaFeatureScaffold(title = stringResource(R.string.tile_sendika), onBack = onBack) {
        SendikaPanelBody()
    }
}

@Composable
fun SendikaPanelBody() {
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    val onPurchase = rememberPremiumPurchaseAction()
    val onRestore = rememberPremiumRestoreAction()
    val billing = entitlement as? BillingEntitlementRepository
    val statusMessage = billing?.lastMessage()

    Text(
        text = if (isPremium) {
            stringResource(R.string.premium_status_active)
        } else {
            stringResource(R.string.premium_status_free)
        },
        style = MaterialTheme.typography.titleMedium,
        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.sendika_panel_body),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!isPremium) {
        BaremPremiumUpsell(onPurchase = onPurchase, onRestore = onRestore)
    }
    if (!statusMessage.isNullOrBlank()) {
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (BuildConfig.DEBUG) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        Text(
            text = stringResource(R.string.premium_debug_section),
            style = MaterialTheme.typography.labelMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { entitlement.grantDebugPremium() }) {
                Text(stringResource(R.string.premium_debug_grant))
            }
            OutlinedButton(onClick = { entitlement.revokeDebugPremium() }) {
                Text(stringResource(R.string.premium_debug_revoke))
            }
        }
        Text(
            text = stringResource(
                R.string.premium_product_id_hint,
                BillingEntitlementRepository.PRODUCT_PREMIUM,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PlaceholderWorkerScreen(
    title: String,
    onBack: () -> Unit,
) {
    SendikaFeatureScaffold(title = title, onBack = onBack) {
        Text(
            text = stringResource(R.string.placeholder_worker_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RealBarChart(labels: List<String>, normalized: List<Float>, raw: List<Float>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        labels.forEachIndexed { index, label ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height((160 * normalized[index]).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (index == valuesLastPeak(raw)) AccentGold else PrimaryBlue,
                        ),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun valuesLastPeak(raw: List<Float>): Int {
    val max = raw.maxOrNull() ?: return raw.lastIndex
    return raw.indexOfLast { it == max }.coerceAtLeast(0)
}

private fun formatTry(value: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = 2
    fmt.maximumFractionDigits = 2
    return "${fmt.format(value)} ₺"
}

private fun formatNum(value: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = 1
    fmt.maximumFractionDigits = 1
    return fmt.format(value)
}

private fun formatFx(value: Double, fractionDigits: Int, suffix: String): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = fractionDigits
    fmt.maximumFractionDigits = fractionDigits
    return "${fmt.format(value)} $suffix"
}

private fun formatUsd(value: Double): String = formatFx(value, 2, "$")

private fun formatEur(value: Double): String = formatFx(value, 2, "€")

private fun formatGoldQuarter(value: Double): String = formatFx(value, 2, "çeyrek")
