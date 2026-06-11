package tr.erdaldemir.barem.ui.screens



import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.grid.GridCells

import androidx.compose.foundation.lazy.grid.GridItemSpan

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

import androidx.compose.foundation.lazy.grid.items

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text

import androidx.compose.material3.TopAppBar

import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import tr.erdaldemir.barem.R

import tr.erdaldemir.barem.domain.model.HomeRoute

import tr.erdaldemir.barem.domain.model.HomeTile

import tr.erdaldemir.barem.ui.components.BaremHomePrimaryCard

import tr.erdaldemir.barem.ui.components.BaremLastCalcHomeCard

import tr.erdaldemir.barem.ui.premium.rememberEntitlement



private const val GRID_UNIT_COLUMNS = 6

private const val PRIMARY_SPAN = 3



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun WelcomeScreen(

    onNavigate: (HomeRoute) -> Unit,

    onOpenLastResult: () -> Unit = {},

    onOpenLastCharts: () -> Unit = {},

) {

    val entitlement = rememberEntitlement()

    val isPremium by entitlement.isPremium.collectAsStateWithLifecycle()



    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(stringResource(R.string.app_name))

                        Text(

                            text = stringResource(R.string.app_subtitle),

                            style = MaterialTheme.typography.bodyMedium,

                            color = MaterialTheme.colorScheme.onSurfaceVariant,

                        )

                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = MaterialTheme.colorScheme.background,

                ),

            )

        },

        containerColor = MaterialTheme.colorScheme.background,

    ) { padding ->

        LazyVerticalGrid(

            columns = GridCells.Fixed(GRID_UNIT_COLUMNS),

            modifier = Modifier

                .fillMaxSize()

                .padding(padding),

            contentPadding = PaddingValues(16.dp),

            horizontalArrangement = Arrangement.spacedBy(12.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp),

        ) {

            item(span = { GridItemSpan(GRID_UNIT_COLUMNS) }) {

                Column(modifier = Modifier.padding(bottom = 4.dp)) {

                    Text(

                        text = stringResource(R.string.welcome_select_type),

                        style = MaterialTheme.typography.titleMedium,

                        color = MaterialTheme.colorScheme.onSurface,

                    )

                    Text(

                        text = if (isPremium) {

                            stringResource(R.string.premium_status_active_banner)

                        } else {

                            stringResource(R.string.free_tier_banner)

                        },

                        style = MaterialTheme.typography.labelMedium,

                        color = MaterialTheme.colorScheme.secondary,

                        modifier = Modifier.padding(top = 4.dp),

                    )

                }

            }

            item(span = { GridItemSpan(GRID_UNIT_COLUMNS) }) {

                BaremLastCalcHomeCard(

                    onOpenResult = onOpenLastResult,

                    onOpenCharts = onOpenLastCharts,

                    modifier = Modifier.padding(bottom = 4.dp),

                )

            }

            items(HomeTile.primaryTiles, span = { GridItemSpan(PRIMARY_SPAN) }) { tile ->

                BaremHomePrimaryCard(

                    title = stringResource(tile.titleRes),

                    subtitle = stringResource(tile.subtitleRes),

                    style = tile.style,

                    enabled = tile.enabled,

                    onClick = { onNavigate(tile.route) },

                )

            }

        }

    }

}


