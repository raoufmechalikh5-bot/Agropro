package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class AgriSemanticColors(
    val safe: Color,
    val safeContainer: Color,
    val onSafeContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val water: Color,
    val waterContainer: Color,
    val onWaterContainer: Color,
    val heroScrimBottom: Color
)

private val LightAgriSemanticColors = AgriSemanticColors(
    safe = SafeGreen,
    safeContainer = SafeGreenContainer,
    onSafeContainer = OnSafeGreenContainerLight,
    warning = WarningAmber,
    warningContainer = WarningAmberContainer,
    onWarningContainer = OnWarningAmberContainerLight,
    danger = DangerCrimson,
    dangerContainer = DangerCrimsonContainer,
    onDangerContainer = OnDangerCrimsonContainerLight,
    water = WaterBlue,
    waterContainer = WaterBlueContainer,
    onWaterContainer = OnWaterBlueContainerLight,
    heroScrimBottom = HeroScrimDarkGreen
)

private val DarkAgriSemanticColors = AgriSemanticColors(
    safe = SafeGreenDark,
    safeContainer = SafeGreenContainerDark,
    onSafeContainer = OnSafeGreenContainerDark,
    warning = WarningAmberDark,
    warningContainer = WarningAmberContainerDark,
    onWarningContainer = OnWarningAmberContainerDark,
    danger = DangerCrimsonDark,
    dangerContainer = DangerCrimsonContainerDark,
    onDangerContainer = OnDangerCrimsonContainerDark,
    water = WaterBlueDark,
    waterContainer = WaterBlueContainerDark,
    onWaterContainer = OnWaterBlueContainerDark,
    heroScrimBottom = HeroScrimDarkGreen
)

val LocalAgriSemanticColors = staticCompositionLocalOf { LightAgriSemanticColors }

object AgriTheme {
    val semantic: AgriSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAgriSemanticColors.current
}

private val AgriProDarkColorScheme = darkColorScheme(
    primary = SproutPrimaryDark,
    onPrimary = OnSproutPrimaryDark,
    primaryContainer = SproutContainerDark,
    onPrimaryContainer = OnSproutContainerDark,
    secondary = TerracottaSecondaryDark,
    onSecondary = OnTerracottaSecondaryDark,
    secondaryContainer = TerracottaContainerDark,
    onSecondaryContainer = OnTerracottaContainerDark,
    tertiary = HarvestGoldTertiaryDark,
    onTertiary = OnHarvestGoldTertiaryDark,
    tertiaryContainer = HarvestGoldContainerDark,
    onTertiaryContainer = OnHarvestGoldContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val AgriProLightColorScheme = lightColorScheme(
    primary = ForestPrimaryLight,
    onPrimary = OnForestPrimaryLight,
    primaryContainer = ForestContainerLight,
    onPrimaryContainer = OnForestContainerLight,
    secondary = TerracottaSecondaryLight,
    onSecondary = OnTerracottaSecondaryLight,
    secondaryContainer = TerracottaContainerLight,
    onSecondaryContainer = OnTerracottaContainerLight,
    tertiary = HarvestGoldTertiaryLight,
    onTertiary = OnHarvestGoldTertiaryLight,
    tertiaryContainer = HarvestGoldContainerLight,
    onTertiaryContainer = OnHarvestGoldContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

val AgriProShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AgriProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AgriProDarkColorScheme else AgriProLightColorScheme
    val semanticColors = if (darkTheme) DarkAgriSemanticColors else LightAgriSemanticColors

    CompositionLocalProvider(LocalAgriSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AgriProTypography,
            shapes = AgriProShapes,
            content = content
        )
    }
}
