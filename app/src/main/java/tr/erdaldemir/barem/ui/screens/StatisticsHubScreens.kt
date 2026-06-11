package tr.erdaldemir.barem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.data.engine.ExcelEngineRepository
import tr.erdaldemir.barem.data.engine.YearlyDataRepository
import tr.erdaldemir.barem.domain.analytics.EngineStatisticsBuilder
import tr.erdaldemir.barem.domain.model.HomeRoute
import tr.erdaldemir.barem.domain.model.StatKind
import tr.erdaldemir.barem.domain.model.StatisticsCatalog
import tr.erdaldemir.barem.domain.model.StatisticsNavStore
import tr.erdaldemir.barem.ui.components.BaremSelectionCard
import tr.erdaldemir.barem.ui.components.BaremStatPairRow
import tr.erdaldemir.barem.ui.premium.rememberSessionYearlySeries
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GeneralStatisticsHubScreen(
    onBack: () -> Unit,
    onOpenRoute: (HomeRoute) -> Unit,
) {
    SendikaFeatureScaffold(
        title = stringResource(R.string.nav_genel_istatistik),
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.stat_hub_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatisticsCatalog.menuItems.forEach { item ->
            BaremSelectionCard(
                title = stringResource(item.titleRes),
                subtitle = stringResource(item.subtitleRes),
                selected = false,
                onClick = {
                    StatisticsNavStore.selectedKind = item.kind
                    when (item.kind) {
                        StatKind.SALARY_TABLE -> onOpenRoute(HomeRoute.StatSalaryTable)
                        else -> onOpenRoute(HomeRoute.StatDetail)
                    }
                },
            )
        }
    }
}

@Composable
fun StatDetailScreen(
    onBack: () -> Unit,
    onOpenCharts: () -> Unit,
) {
    val kind = StatisticsNavStore.selectedKind
    val context = LocalContext.current
    val yearlyRepo = remember { YearlyDataRepository(context.applicationContext) }
    val excelRepo = remember { ExcelEngineRepository(context.applicationContext) }
    val yearly = rememberSessionYearlySeries()
    var monthlyView by remember { mutableStateOf(false) }

    val title = when (kind) {
        StatKind.DOLLAR -> stringResource(R.string.stat_menu_dollar)
        StatKind.GOLD -> stringResource(R.string.stat_menu_gold)
        StatKind.ASGARI -> stringResource(R.string.stat_menu_asgari)
        StatKind.MEMUR_RAISE -> stringResource(R.string.stat_menu_memur_raise)
        StatKind.ASGARI_RAISE -> stringResource(R.string.stat_menu_asgari_raise)
        StatKind.PRIM_RATES -> stringResource(R.string.stat_menu_prim)
        StatKind.GV_BRACKETS -> stringResource(R.string.stat_menu_gv)
        else -> stringResource(R.string.nav_genel_istatistik)
    }

    val showChart = kind != null && kind != StatKind.PRIM_RATES && kind != StatKind.GV_BRACKETS

    SendikaFeatureScaffold(title = title, onBack = onBack) {
        if (kind == null) {
            Text(stringResource(R.string.empty_step_message))
            return@SendikaFeatureScaffold
        }

        if (showChart) {
            Button(
                onClick = {
                    StatisticsNavStore.chartTabIndex = chartTabFor(kind)
                    onOpenCharts()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.stat_show_chart))
            }
        }

        if (kind != StatKind.PRIM_RATES && kind != StatKind.GV_BRACKETS) {
            OutlinedButton(
                onClick = { monthlyView = !monthlyView },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (monthlyView) {
                        stringResource(R.string.stat_show_yearly)
                    } else {
                        stringResource(R.string.stat_show_monthly)
                    },
                )
            }
        }

        when (kind) {
            StatKind.DOLLAR -> {
                val rows = if (yearly.isNotEmpty()) {
                    EngineStatisticsBuilder.dollarFromSalary(yearly)
                } else {
                    EngineStatisticsBuilder.usdRates(yearlyRepo)
                }
                StatValueList(rows, monthlyView, suffix = "$")
            }
            StatKind.GOLD -> {
                val rows = if (yearly.isNotEmpty()) {
                    EngineStatisticsBuilder.goldFromSalary(yearly)
                } else {
                    EngineStatisticsBuilder.goldPerGram(yearlyRepo)
                }
                StatValueList(rows, monthlyView, suffix = "çeyrek")
            }
            StatKind.ASGARI -> {
                StatValueList(
                    EngineStatisticsBuilder.netAsgari(excelRepo, yearlyRepo.activeYear()),
                    monthlyView,
                    suffix = "₺",
                )
            }
            StatKind.MEMUR_RAISE -> {
                StatValueList(
                    EngineStatisticsBuilder.memurRaisePct(yearly, excelRepo),
                    monthlyView = false,
                    suffix = "%",
                    fractionDigits = 1,
                )
            }
            StatKind.ASGARI_RAISE -> {
                val asgari = EngineStatisticsBuilder.netAsgari(excelRepo, yearlyRepo.activeYear())
                StatValueList(
                    EngineStatisticsBuilder.asgariRaisePct(asgari),
                    monthlyView = false,
                    suffix = "%",
                    fractionDigits = 1,
                )
            }
            StatKind.PRIM_RATES -> {
                EngineStatisticsBuilder.primRates(yearlyRepo).forEach { (label, rate) ->
                    BaremStatPairRow(
                        label = "SGK $label",
                        value = "${formatStat(rate * 100, 2)}%",
                    )
                }
            }
            StatKind.GV_BRACKETS -> {
                val labels = EngineStatisticsBuilder.gvBracketLabels()
                val values = EngineStatisticsBuilder.gvBrackets(yearlyRepo)
                labels.forEachIndexed { index, label ->
                    val v = values.getOrNull(index)
                    BaremStatPairRow(
                        label = label,
                        value = v?.let { formatStat(it, 0) } ?: "—",
                    )
                }
            }
            StatKind.SALARY_TABLE -> Unit
        }
    }
}

@Composable
private fun StatValueList(
    rows: List<tr.erdaldemir.barem.domain.analytics.YearValueRow>,
    monthlyView: Boolean,
    suffix: String,
    fractionDigits: Int = 2,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (rows.isEmpty()) {
            Text(
                text = stringResource(R.string.calc_required_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }
        rows.sortedByDescending { it.year }.forEach { row ->
            val value = if (monthlyView) row.monthlyValue else row.yearlyValue
            BaremStatPairRow(
                label = row.year.toString(),
                value = "${formatStat(value, fractionDigits)} $suffix",
            )
        }
    }
}

private fun chartTabFor(kind: StatKind): Int = when (kind) {
    StatKind.DOLLAR -> 1
    StatKind.GOLD -> 3
    StatKind.MEMUR_RAISE -> 4
    StatKind.ASGARI_RAISE -> 4
    StatKind.ASGARI -> 0
    else -> 0
}

private fun formatStat(value: Double, digits: Int): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = digits
    fmt.maximumFractionDigits = digits
    return fmt.format(value)
}
