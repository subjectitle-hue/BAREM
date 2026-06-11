package tr.erdaldemir.barem

import android.app.Application
import android.content.Context
import tr.erdaldemir.barem.data.billing.BillingEntitlementRepository
import tr.erdaldemir.barem.domain.entitlement.EntitlementRepository

class BaremApplication : Application() {

    lateinit var entitlement: EntitlementRepository
        private set

    override fun onCreate() {
        super.onCreate()
        entitlement = BillingEntitlementRepository(this)
    }

    companion object {
        fun from(context: Context): BaremApplication =
            context.applicationContext as BaremApplication
    }
}
