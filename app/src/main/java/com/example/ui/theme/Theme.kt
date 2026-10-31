package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeColors

data class CustomCyberColors(
    val void: Color,
    val panel: Color,
    val ink: Color,
    val muted: Color,
    val cyan: Color,
    val magenta: Color,
    val line: Color
)

val LocalCyberColors = staticCompositionLocalOf {
    CustomCyberColors(
        void = VoidDark,
        panel = PanelDark,
        ink = InkElectric,
        muted = SlateMuted,
        cyan = CyanNeon,
        magenta = MagentaNeon,
        line = CyberLine
    )
}

@Composable
fun WessPlusTheme(
    themeColors: ThemeColors = ThemeColors.Cyber,
    content: @Composable () -> Unit
) {
    val voidCol = parseHexColor(themeColors.voidHex, VoidDark)
    val panelCol = parseHexColor(themeColors.panelHex, PanelDark)
    val inkCol = parseHexColor(themeColors.inkHex, InkElectric)
    val mutedCol = parseHexColor(themeColors.mutedHex, SlateMuted)
    val cyanCol = parseHexColor(themeColors.cyanHex, CyanNeon)
    val magentaCol = parseHexColor(themeColors.magentaHex, MagentaNeon)
    val lineCol = cyanCol.copy(alpha = 0.25f)

    val customColors = CustomCyberColors(
        void = voidCol,
        panel = panelCol,
        ink = inkCol,
        muted = mutedCol,
        cyan = cyanCol,
        magenta = magentaCol,
        line = lineCol
    )

    val colorScheme = darkColorScheme(
        primary = cyanCol,
        onPrimary = voidCol,
        primaryContainer = cyanCol.copy(alpha = 0.15f),
        onPrimaryContainer = cyanCol,
        secondary = magentaCol,
        onSecondary = voidCol,
        secondaryContainer = magentaCol.copy(alpha = 0.15f),
        onSecondaryContainer = magentaCol,
        background = voidCol,
        onBackground = inkCol,
        surface = panelCol,
        onSurface = inkCol,
        surfaceVariant = panelCol.copy(alpha = 0.85f),
        onSurfaceVariant = mutedCol,
        outline = lineCol
    )

    CompositionLocalProvider(LocalCyberColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
