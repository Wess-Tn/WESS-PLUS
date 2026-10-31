package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val CyanNeon = Color(0xFF00F0FF)
val MagentaNeon = Color(0xFFFF2A6D)
val VoidDark = Color(0xFF05060A)
val PanelDark = Color(0xFF0B0E17)
val InkElectric = Color(0xFFD8F7FF)
val SlateMuted = Color(0xFF6F7FA0)
val CyberLine = Color(0x3800F0FF)
val CyberLineMagenta = Color(0x38FF2A6D)

val YellowNeon = Color(0xFFFCEE0A)
val GreenNeon = Color(0xFF7CFF6B)
val PurpleNeon = Color(0xFFB38CFF)

fun parseHexColor(hex: String, fallback: Color = CyanNeon): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> 0xFF000000.toInt() or clean.toInt(16)
            8 -> clean.toLong(16).toInt()
            else -> fallback.value.toInt()
        }
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}
