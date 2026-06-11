package tr.erdaldemir.barem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.HomeRoute
import tr.erdaldemir.barem.ui.components.BaremSelectionCard

@Composable
fun AccountHubScreen(
    onBack: () -> Unit,
    onOpenRoute: (HomeRoute) -> Unit,
) {
    SendikaFeatureScaffold(
        title = stringResource(R.string.nav_hesap),
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.account_hub_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SendikaPanelBody()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = stringResource(R.string.account_hub_tools_title),
            style = MaterialTheme.typography.titleSmall,
        )
        HubLinkCard(
            title = stringResource(R.string.tile_grafik),
            subtitle = stringResource(R.string.tile_grafik_sub),
            onClick = { onOpenRoute(HomeRoute.SalaryCharts) },
        )
        HubLinkCard(
            title = stringResource(R.string.tile_yillik),
            subtitle = stringResource(R.string.tile_yillik_sub),
            onClick = { onOpenRoute(HomeRoute.YearlyAverage) },
        )
        HubLinkCard(
            title = stringResource(R.string.tile_fx),
            subtitle = stringResource(R.string.tile_fx_sub),
            onClick = { onOpenRoute(HomeRoute.GoldDollar) },
        )
    }
}

@Composable
fun VeriHubScreen(
    onBack: () -> Unit,
    onOpenRoute: (HomeRoute) -> Unit,
) {
    SendikaFeatureScaffold(
        title = stringResource(R.string.nav_veri),
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.veri_hub_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HubLinkCard(
            title = stringResource(R.string.veri_hub_yearly_title),
            subtitle = stringResource(R.string.veri_hub_yearly_sub),
            onClick = { onOpenRoute(HomeRoute.YearlyAverage) },
        )
        HubLinkCard(
            title = stringResource(R.string.veri_hub_fx_title),
            subtitle = stringResource(R.string.veri_hub_fx_sub),
            onClick = { onOpenRoute(HomeRoute.GoldDollar) },
        )
        HubLinkCard(
            title = stringResource(R.string.veri_hub_raw_title),
            subtitle = stringResource(R.string.veri_hub_raw_sub),
            onClick = { onOpenRoute(HomeRoute.YearlyAverage) },
        )
    }
}

@Composable
private fun HubLinkCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    BaremSelectionCard(
        title = title,
        subtitle = subtitle,
        selected = false,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    )
}
