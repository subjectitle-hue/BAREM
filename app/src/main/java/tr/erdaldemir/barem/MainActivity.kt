package tr.erdaldemir.barem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tr.erdaldemir.barem.domain.PremiumAccess
import tr.erdaldemir.barem.ui.BaremApp
import tr.erdaldemir.barem.ui.premium.ProvideEntitlement
import tr.erdaldemir.barem.ui.theme.BaremTheme

class MainActivity : ComponentActivity() {

    private val entitlement by lazy { BaremApplication.from(this).entitlement }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PremiumAccess.bind(entitlement)
        setContent {
            BaremTheme {
                ProvideEntitlement(entitlement) {
                    BaremApp()
                }
            }
        }
    }
}
