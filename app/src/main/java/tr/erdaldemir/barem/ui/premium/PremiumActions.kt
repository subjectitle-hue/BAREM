package tr.erdaldemir.barem.ui.premium

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberPremiumPurchaseAction(): () -> Unit {
    val context = LocalContext.current
    val entitlement = rememberEntitlement()
    return remember(entitlement, context) {
        {
            val activity = context as? Activity
            if (activity != null) {
                entitlement.launchPremiumPurchase(activity)
            }
        }
    }
}

@Composable
fun rememberPremiumRestoreAction(): () -> Unit {
    val entitlement = rememberEntitlement()
    return remember(entitlement) {
        { entitlement.refreshPurchases() }
    }
}
