package com.fitifinance.comrade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.fitifinance.comrade.engine.ThemeMode

private val CampusScheme = lightColorScheme(
    primary = CampusPrimary,
    secondary = CampusSecondary,
    background = CampusBackground,
    surface = CampusSurface,
    onPrimary = CampusOnPrimary
)

private val BarScheme = darkColorScheme(
    primary = BarPrimary,
    secondary = BarAccentCyan,
    background = BarBackground,
    surface = BarSurface,
    onPrimary = BarOnPrimary
)

private val KibandaScheme = lightColorScheme(
    primary = KibandaPrimary,
    secondary = KibandaSecondary,
    background = KibandaBackground,
    surface = KibandaSurface,
    onPrimary = KibandaOnPrimary
)

/**
 * Wraps content in the color scheme matching the current Location-Aware
 * Context Engine mode: Campus (Navy/Slate), Bar (Neon Dark/AMOLED), or
 * Kibanda (Warm Amber/Green).
 */
@Composable
fun FitiFinanceTheme(themeMode: ThemeMode = ThemeMode.CAMPUS, content: @Composable () -> Unit) {
    val colorScheme = when (themeMode) {
        ThemeMode.CAMPUS -> CampusScheme
        ThemeMode.BAR -> BarScheme
        ThemeMode.KIBANDA -> KibandaScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitiTypography,
        content = content
    )
}
