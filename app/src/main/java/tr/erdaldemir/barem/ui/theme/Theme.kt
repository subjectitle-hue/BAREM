package tr.erdaldemir.barem.ui.theme



import android.app.Activity

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.lightColorScheme

import androidx.compose.runtime.Composable

import androidx.compose.runtime.SideEffect

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.toArgb

import androidx.compose.ui.platform.LocalView

import androidx.core.view.WindowCompat



private val BaremColorScheme = lightColorScheme(

    primary = PrimaryBlue,

    onPrimary = Color.White,

    primaryContainer = Color(0xFFDBEAFE),

    onPrimaryContainer = Color(0xFF0C4A6E),

    secondary = Color(0xFFB45309),

    onSecondary = Color.White,

    secondaryContainer = Color(0xFFFFEDD5),

    onSecondaryContainer = Color(0xFF7C2D12),

    tertiary = SuccessGreen,

    background = Color(0xFFF8FAFC),

    surface = Color.White,

    surfaceVariant = Color(0xFFE2E8F0),

    onBackground = Color(0xFF0F172A),

    onSurface = Color(0xFF0F172A),

    onSurfaceVariant = Color(0xFF475569),

    outline = Color(0xFF94A3B8),

)



@Composable

fun BaremTheme(content: @Composable () -> Unit) {

    val colorScheme = BaremColorScheme

    val view = LocalView.current

    if (!view.isInEditMode) {

        SideEffect {

            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.background.toArgb()

            window.navigationBarColor = colorScheme.surface.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true

        }

    }

    MaterialTheme(

        colorScheme = colorScheme,

        typography = Typography,

        content = content,

    )

}


