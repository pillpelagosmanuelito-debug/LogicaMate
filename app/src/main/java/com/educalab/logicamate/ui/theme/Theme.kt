package com.educalab.logicamate.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LogicaMateColorScheme = darkColorScheme(
    primary = RuneGold,
    onPrimary = StoneDeep,
    secondary = CrystalTeal,
    onSecondary = StoneDeep,
    tertiary = EmberCoral,
    background = StoneDeep,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardElevated,
    onSurfaceVariant = TextSecondary,
    error = EmberCoral,
)

private val LogicaMateShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun LogicaMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LogicaMateColorScheme,
        typography = LogicaMateTypography,
        shapes = LogicaMateShapes,
        content = content,
    )
}
