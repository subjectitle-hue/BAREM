package tr.erdaldemir.barem.ui.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import tr.erdaldemir.barem.domain.entitlement.EntitlementRepository

val LocalEntitlement = staticCompositionLocalOf<EntitlementRepository> {
    error("EntitlementRepository not provided")
}

@Composable
fun ProvideEntitlement(
    entitlement: EntitlementRepository,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalEntitlement provides entitlement) {
        content()
    }
}

@Composable
fun rememberEntitlement(): EntitlementRepository = LocalEntitlement.current
