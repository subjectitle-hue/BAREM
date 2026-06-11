package tr.erdaldemir.barem.ui.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.ui.theme.AccentGold

@Composable
fun BaremPremiumUpsell(
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 4.dp else 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.premium_upsell_title),
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = AccentGold,
        )
        Text(
            text = stringResource(R.string.premium_upsell_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!compact) {
            Text(
                text = stringResource(R.string.premium_upsell_features),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(onClick = onPurchase, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.premium_buy))
        }
        OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.premium_restore))
        }
    }
}

@Composable
fun BaremPremiumGate(
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
    previewContent: (@Composable () -> Unit)? = null,
    premiumContent: @Composable () -> Unit,
) {
    val entitlement = rememberEntitlement()
    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()
    if (isPremium) {
        premiumContent()
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            previewContent?.invoke()
            BaremPremiumUpsell(onPurchase = onPurchase, onRestore = onRestore)
        }
    }
}
