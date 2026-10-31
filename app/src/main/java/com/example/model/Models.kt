package com.example.model

import java.util.UUID

enum class AppMode {
    CHAT,
    IMAGE,
    BIOSCIENCE,
    AI_TOOLS
}

enum class QualityTier {
    QUICK,
    BALANCED,
    THOROUGH
}

data class WebLink(
    val title: String,
    val url: String
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val links: List<WebLink> = emptyList(),
    val imageUri: String? = null,
    val mediaKind: String? = null, // "image" or "video"
    val isThinking: Boolean = false,
    val isError: Boolean = false
)

data class ChatThread(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage> = emptyList()
)

data class SiteShortcut(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val mark: String,
    val colorHex: String,
    val category: String = "General"
)

data class ThemeColors(
    val name: String,
    val voidHex: String,
    val panelHex: String,
    val inkHex: String,
    val mutedHex: String,
    val cyanHex: String,
    val magentaHex: String
) {
    companion object {
        // Exclusive Official Theme for WESS PLUS
        val WessPlusSignature = ThemeColors(
            name = "WESS PLUS Signature",
            voidHex = "#030408",
            panelHex = "#070B14",
            inkHex = "#E0F8FF",
            mutedHex = "#5E7599",
            cyanHex = "#00F0FF",
            magentaHex = "#FF1F6D"
        )

        val Cyber = ThemeColors(
            name = "Cyber",
            voidHex = "#05060A",
            panelHex = "#0B0E17",
            inkHex = "#D8F7FF",
            mutedHex = "#6F7FA0",
            cyanHex = "#00F0FF",
            magentaHex = "#FF2A6D"
        )
        val Ocean = ThemeColors(
            name = "Ocean",
            voidHex = "#04141F",
            panelHex = "#0A2233",
            inkHex = "#E6F7FF",
            mutedHex = "#7AA3B8",
            cyanHex = "#38D9F5",
            magentaHex = "#4F8CFF"
        )
        val Sunset = ThemeColors(
            name = "Sunset",
            voidHex = "#1A0A12",
            panelHex = "#2A1020",
            inkHex = "#FFE9DC",
            mutedHex = "#B88A7E",
            cyanHex = "#FFB36B",
            magentaHex = "#FF4F7B"
        )
        val Matrix = ThemeColors(
            name = "Matrix",
            voidHex = "#020602",
            panelHex = "#071207",
            inkHex = "#C8FFC8",
            mutedHex = "#5F8F5F",
            cyanHex = "#39FF7A",
            magentaHex = "#B6FF3B"
        )
        val Paper = ThemeColors(
            name = "Paper",
            voidHex = "#F5F1E8",
            panelHex = "#FFFDF7",
            inkHex = "#1D1B16",
            mutedHex = "#7B7566",
            cyanHex = "#2F5D50",
            magentaHex = "#B5462F"
        )

        val Gemini = ThemeColors(
            name = "Gemini AI",
            voidHex = "#0E121A",
            panelHex = "#171D2B",
            inkHex = "#E3E8F5",
            mutedHex = "#8E99B0",
            cyanHex = "#7DACFC",
            magentaHex = "#C58AF9"
        )

        val Presets = listOf(WessPlusSignature, Gemini, Cyber, Ocean, Sunset, Matrix, Paper)
    }
}

data class AppSettings(
    val scanlines: Boolean = true,
    val reducedMotion: Boolean = false,
    val speakReplies: Boolean = false,
    val qualityTier: QualityTier = QualityTier.BALANCED,
    val languageCode: String = "ar", // Default to user's native Arabic
    val activeTheme: ThemeColors = ThemeColors.WessPlusSignature,
    val themeDarkness: Float = 0.45f,
    val themeBlur: Float = 0f,
    val isIncognito: Boolean = false,
    // Chrome-like advanced settings
    val searchEngine: String = "Google", // Google, DuckDuckGo, Bing, Ecosia, WESS Quantum
    val safeBrowsing: Boolean = true,
    val blockThirdPartyCookies: Boolean = true,
    val httpsOnly: Boolean = true,
    val doNotTrack: Boolean = true,
    val javascriptEnabled: Boolean = true,
    val popupsBlocked: Boolean = true,
    val desktopModeDefault: Boolean = false,
    val preloadPages: Boolean = true,
    val memorySaver: Boolean = true
)

// Advanced AI Tool Models
data class AiToolItem(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val iconName: String,
    val promptTemplate: String,
    val sampleInput: String
)

// QR Scan & Generation Result
data class QrResult(
    val rawText: String,
    val isUrl: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Bioscience Data Models
data class DnaAnalysisResult(
    val length: Int,
    val gcCount: Int,
    val gcPercent: Float,
    val atPercent: Float,
    val molecularWeightKDa: Float,
    val meltingTempTm: Float,
    val reverseComplement: String,
    val transcribedRna: String,
    val translatedProteinFrame1: String,
    val translatedProteinFrame2: String,
    val translatedProteinFrame3: String
)

data class ProteinAnalysisResult(
    val length: Int,
    val molecularWeightDa: Float,
    val isoelectricPoint: Float,
    val netChargePh74: Float,
    val composition: Map<Char, Int>,
    val hydropathyScores: List<Float>
)

data class CrisprGuideResult(
    val protospacer: String,
    val pam: String,
    val strand: String, // "+" or "-"
    val startPosition: Int,
    val gcPercent: Float,
    val qualityScore: Int
)

data class LabDilutionResult(
    val formula: String,
    val stockVolumeNeeded: Float,
    val diluentVolumeNeeded: Float,
    val explanation: String
)

data class PcrMixComponent(
    val name: String,
    val stockConcentration: String,
    val finalConcentration: String,
    val volumePerReactionUl: Float,
    val totalVolumeUl: Float
)

data class BioReferenceGuide(
    val title: String,
    val category: String,
    val summary: String,
    val details: String
)
