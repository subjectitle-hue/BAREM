package tr.erdaldemir.barem.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tr.erdaldemir.barem.R
import tr.erdaldemir.barem.domain.model.HomeRoute
import tr.erdaldemir.barem.ui.theme.AccentGold
import tr.erdaldemir.barem.ui.theme.PrimaryBlue

private data class BottomNavItem(
    val route: HomeRoute,
    val labelRes: Int,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeRoute.AccountHub, R.string.nav_hesap, Icons.Default.AccountBalanceWallet),
    BottomNavItem(HomeRoute.GorusHub, R.string.nav_gorus, Icons.AutoMirrored.Filled.ShowChart),
    BottomNavItem(HomeRoute.History, R.string.nav_gecmis, Icons.Default.History),
    BottomNavItem(HomeRoute.Compare, R.string.nav_genel_istatistik, Icons.Default.CompareArrows),
)

@Composable
fun BaremBottomBar(
    currentRoute: HomeRoute?,
    onNavigateHome: () -> Unit,
    onNavigateTool: (HomeRoute) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PrimaryBlue,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute == item.route) {
                        onNavigateHome()
                    } else {
                        onNavigateTool(item.route)
                    }
                },
                colors = bottomNavColors(),
            )
        }
    }
}

@Composable
private fun bottomNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AccentGold,
    selectedTextColor = AccentGold,
    unselectedIconColor = Color(0xFF94A3B8),
    unselectedTextColor = Color(0xFF94A3B8),
    indicatorColor = AccentGold.copy(alpha = 0.15f),
)
