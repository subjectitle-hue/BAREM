package tr.erdaldemir.barem.ui.memur

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.SalaryResult
import androidx.compose.foundation.layout.padding
import tr.erdaldemir.barem.ui.components.BaremMiniChoiceChip
import tr.erdaldemir.barem.ui.theme.AccentBurgundy
import tr.erdaldemir.barem.ui.theme.AccentGold
import tr.erdaldemir.barem.ui.theme.PrimaryBlue
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: SalaryResult,
    onOpenBordro: () -> Unit,
    onOpenSalaryStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currency by remember { mutableStateOf(ResultCurrency.TL) }
    val monthly = ResultCurrencyData.monthlyValues(result, currency)
    val stats = ResultCurrencyData.periodStats(result, currency)
    val labels = result.monthlyLabels

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ResultCurrencySelector(
            selected = currency,
            onSelect = { currency = it },
        )

        if (monthly.isNotEmpty()) {
            monthly.chunked(3).forEachIndexed { rowIndex, rowValues ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowValues.forEachIndexed { colIndex, value ->
                        val index = rowIndex * 3 + colIndex
                        val label = labels.getOrElse(index) { "${index + 1}" }
                        ResultMonthCell(
                            month = label,
                            amount = formatResultAmount(value, currency),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - rowValues.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        ResultStatBox(
            title = stringResource(R.string.result_yearly_avg_label),
            value = formatResultAmount(stats.yearlyAvg, currency),
            highlight = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ResultHubTile(
            title = stringResource(R.string.result_hub_bordro),
            onClick = onOpenBordro,
            modifier = Modifier.fillMaxWidth(),
        )
        ResultHubTile(
            title = stringResource(R.string.result_hub_salary_stats),
            onClick = onOpenSalaryStats,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ResultCurrencySelector(
    selected: ResultCurrency,
    onSelect: (ResultCurrency) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResultCurrency.selectorOrder.forEach { option ->
            BaremMiniChoiceChip(
                label = option.label,
                selected = selected == option,
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ResultMonthCell(
    month: String,
    amount: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = month,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ResultStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) AccentBurgundy else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = if (highlight) 2.dp else 1.dp,
            color = if (highlight) AccentGold else PrimaryBlue.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (highlight) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ResultHubTile(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
        )
    }
}

internal fun formatResultAmount(value: Double, currency: ResultCurrency): String {
    val fmt = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    fmt.minimumFractionDigits = 2
    fmt.maximumFractionDigits = 2
    val formatted = fmt.format(value)
    return when (currency) {
        ResultCurrency.TL -> "$formatted ₺"
        ResultCurrency.USD -> "$$formatted"
        ResultCurrency.EUR -> "$formatted €"
        ResultCurrency.GOLD -> "$formatted çeyrek"
    }
}
