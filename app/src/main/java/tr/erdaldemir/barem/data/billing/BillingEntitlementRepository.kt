package tr.erdaldemir.barem.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tr.erdaldemir.barem.BuildConfig
import tr.erdaldemir.barem.domain.entitlement.EntitlementRepository

/**
 * Google Play Billing iskeleti.
 * Play Console'da [PRODUCT_PREMIUM] tanımlanana kadar satın alma başarısız olabilir;
 * debug'da [grantDebugPremium] ile test edilir.
 */
class BillingEntitlementRepository(
    context: Context,
) : EntitlementRepository, PurchasesUpdatedListener, BillingClientStateListener {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _isPremium = MutableStateFlow(readLocalPremium())
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private var cachedProduct: ProductDetails? = null
    private var lastBillingMessage: String? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    init {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            refreshPurchases()
            prefetchProduct()
        }
    }

    override fun onBillingServiceDisconnected() {
        // BillingClient otomatik yeniden bağlanır; bir sonraki refresh yeterli.
    }

    override fun refreshPurchases() {
        if (!billingClient.isReady) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) { result, purchases ->
            val owned = result.responseCode == BillingClient.BillingResponseCode.OK &&
                purchases.any { it.products.contains(PRODUCT_PREMIUM) && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            applyPremiumState(playOwned = owned)
        }
    }

    override fun launchPremiumPurchase(activity: Activity) {
        if (!billingClient.isReady) {
            lastBillingMessage = "Play Store bağlantısı henüz hazır değil."
            return
        }
        val product = cachedProduct
        if (product == null) {
            lastBillingMessage =
                "Ürün Play Console'da yapılandırılmamış ($PRODUCT_PREMIUM). Debug'da test premium kullanın."
            prefetchProduct()
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build(),
                ),
            )
            .build()
        val launchResult = billingClient.launchBillingFlow(activity, params)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            lastBillingMessage = "Satın alma başlatılamadı (${launchResult.debugMessage})"
        }
    }

    override fun grantDebugPremium() {
        if (!BuildConfig.DEBUG) return
        prefs.edit().putBoolean(KEY_DEBUG_PREMIUM, true).apply()
        applyPremiumState(playOwned = false)
    }

    override fun revokeDebugPremium() {
        if (!BuildConfig.DEBUG) return
        prefs.edit().putBoolean(KEY_DEBUG_PREMIUM, false).apply()
        refreshPurchases()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases.isNullOrEmpty()) {
            if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                lastBillingMessage = null
            } else if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                lastBillingMessage = result.debugMessage
            }
            return
        }
        purchases.forEach { handlePurchase(it) }
    }

    fun lastMessage(): String? = lastBillingMessage

    private fun prefetchProduct() {
        if (!billingClient.isReady) return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_PREMIUM)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                cachedProduct = productDetailsList.firstOrNull()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.products.contains(PRODUCT_PREMIUM)) return
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { ackResult ->
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    applyPremiumState(playOwned = true)
                }
            }
        } else {
            applyPremiumState(playOwned = true)
        }
    }

    private fun applyPremiumState(playOwned: Boolean) {
        val debug = BuildConfig.DEBUG && prefs.getBoolean(KEY_DEBUG_PREMIUM, false)
        val premium = playOwned || debug
        prefs.edit().putBoolean(KEY_LOCAL_PREMIUM, premium).apply()
        _isPremium.value = premium
    }

    private fun readLocalPremium(): Boolean =
        prefs.getBoolean(KEY_LOCAL_PREMIUM, false)

    companion object {
        const val PRODUCT_PREMIUM = "Barem_premium"
        private const val PREFS = "Barem_entitlement"
        private const val KEY_LOCAL_PREMIUM = "local_premium"
        private const val KEY_DEBUG_PREMIUM = "debug_premium"
    }
}
