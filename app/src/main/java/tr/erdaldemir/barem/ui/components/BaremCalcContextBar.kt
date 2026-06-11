package tr.erdaldemir.barem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.session.CalcSessionStore

@Composable
fun BaremCalcContextBar(
    onBackToResult: () -> Unit,
    onEditSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!CalcSessionStore.hasActiveSession()) return
    val kadro = CalcSessionStore.kadroSummaryLine()
    val net = CalcSessionStore.netSummaryLine()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.calc_context_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (kadro != null) {
            Text(
                text = kadro,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (net != null) {
            Text(
                text = net,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.calc_context_yearly_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onBackToResult) {
                Text(stringResource(R.string.calc_context_back_result))
            }
            TextButton(onClick = onEditSelection) {
                Text(stringResource(R.string.calc_context_edit_selection))
            }
        }
    }
}

@Composable
fun BaremLastCalcHomeCard(
    onOpenResult: () -> Unit,
    onOpenCharts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!CalcSessionStore.hasActiveSession()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.last_calc_card_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        CalcSessionStore.kadroSummaryLine()?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
        CalcSessionStore.netSummaryLine()?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenResult) {
                Text(stringResource(R.string.calc_context_back_result))
            }
            TextButton(onClick = onOpenCharts) {
                Text(stringResource(R.string.last_calc_open_charts))
            }
        }
    }
}
