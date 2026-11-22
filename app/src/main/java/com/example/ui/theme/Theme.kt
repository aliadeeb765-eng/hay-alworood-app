package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = DamaskEmerald,
    onPrimary = Color.White,
    primaryContainer = DamaskEmeraldContainer,
    onPrimaryContainer = OnDamaskEmeraldContainer,
    secondary = DamaskRoseTerracotta,
    onSecondary = Color.White,
    secondaryContainer = DamaskRoseContainer,
    onSecondaryContainer = OnDamaskRoseContainer,
    tertiary = DamaskGold,
    onTertiary = Color.White,
    tertiaryContainer = DamaskGoldContainer,
    onTertiaryContainer = OnDamaskGoldContainer,
    background = JasmineCream,
    onBackground = CharcoalInk,
    surface = JasmineCream,
    onSurface = CharcoalInk,
    surfaceVariant = WarmLimestone,
    onSurfaceVariant = MutedOliveText,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = WarmLimestone,
    surfaceContainer = Color(0xFFEFE8DC),
    surfaceContainerHigh = Color(0xFFE9E0D2),
    outline = SoftStoneBorder,
    outlineVariant = Color(0xFFD5CCB9),
    error = StatusCutRed,
    onError = Color.White,
    errorContainer = StatusCutBg,
    onErrorContainer = Color(0xFF5D130B)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmerald,
    onPrimary = DarkOnEmerald,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = DamaskEmeraldContainer,
    secondary = DarkRose,
    onSecondary = Color(0xFF57160D),
    secondaryContainer = DarkRoseContainer,
    onSecondaryContainer = DamaskRoseContainer,
    tertiary = DarkGold,
    onTertiary = Color(0xFF3F2700),
    tertiaryContainer = DarkGoldContainer,
    onTertiaryContainer = DamaskGoldContainer,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB5BEB4),
    surfaceContainerLowest = Color(0xFF0C0F0D),
    surfaceContainerLow = Color(0xFF191F1B),
    surfaceContainer = Color(0xFF202722),
    surfaceContainerHigh = Color(0xFF29322C),
    outline = Color(0xFF3D4740),
    outlineVariant = Color(0xFF2F3832)
)

val HayAlWurudShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HayAlWurudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HayAlWurudTypography,
        shapes = HayAlWurudShapes,
        content = content
    )
}
