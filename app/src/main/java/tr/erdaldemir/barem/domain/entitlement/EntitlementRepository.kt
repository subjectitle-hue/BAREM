package tr.erdaldemir.barem.domain.entitlement

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/** Premium hakları — Google Play Billing + yerel önbellek. */
interface EntitlementRepository {
    val isPremium: StateFlow<Boolean>

    /** Play'den satın almaları yeniden oku. */
    fun refreshPurchases()

    /** Premium ürün satın alma akışını başlatır. */
    fun launchPremiumPurchase(activity: Activity)

    /** Yalnızca debug: IAP simülasyonu. */
    fun grantDebugPremium()

    fun revokeDebugPremium()
}
