package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppSettings
import com.example.model.QualityTier
import com.example.model.ThemeColors
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.theme.LocalCyberColors

private enum class SettingsTab {
    THEMES,
    LANGUAGE,
    SEARCH,
    PRIVACY,
    PERFORMANCE,
    RESET
}

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onTriggerDiscordPlanner: () -> Unit,
    onResetShortcuts: () -> Unit,
    onClearChats: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCyberColors.current
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(SettingsTab.THEMES) }
    val isAr = settings.languageCode == "ar"

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CyberCutShape)
                .background(colors.panel)
                .border(1.dp, colors.cyan, CyberCutShape)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isAr) "⚙️ إعدادات المتصفح والنظام" else "⚙️ CHROME-GRADE SETTINGS",
                            color = colors.cyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.muted
                        )
                    }
                }

                // Category Chips Row (Like Chrome settings categories)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        TabChip(
                            label = if (isAr) "🎨 الثيمات" else "🎨 Themes",
                            isSelected = selectedTab == SettingsTab.THEMES,
                            onClick = { selectedTab = SettingsTab.THEMES }
                        )
                    }
                    item {
                        TabChip(
                            label = if (isAr) "🌐 اللغة" else "🌐 Language",
                            isSelected = selectedTab == SettingsTab.LANGUAGE,
                            onClick = { selectedTab = SettingsTab.LANGUAGE }
                        )
                    }
                    item {
                        TabChip(
                            label = if (isAr) "🔍 محرك البحث" else "🔍 Search",
                            isSelected = selectedTab == SettingsTab.SEARCH,
                            onClick = { selectedTab = SettingsTab.SEARCH }
                        )
                    }
                    item {
                        TabChip(
                            label = if (isAr) "🛡️ الأمان والخصوصية" else "🛡️ Privacy",
                            isSelected = selectedTab == SettingsTab.PRIVACY,
                            onClick = { selectedTab = SettingsTab.PRIVACY }
                        )
                    }
                    item {
                        TabChip(
                            label = if (isAr) "⚡ الأداء والمحتوى" else "⚡ Performance",
                            isSelected = selectedTab == SettingsTab.PERFORMANCE,
                            onClick = { selectedTab = SettingsTab.PERFORMANCE }
                        )
                    }
                    item {
                        TabChip(
                            label = if (isAr) "⚠️ إدارة البيانات" else "⚠️ Data",
                            isSelected = selectedTab == SettingsTab.RESET,
                            onClick = { selectedTab = SettingsTab.RESET }
                        )
                    }
                }

                Divider(color = colors.line, thickness = 0.8.dp)

                // Tab Content
                when (selectedTab) {
                    SettingsTab.THEMES -> {
                        // Theme Presets Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAr) "قوالب الثيمات والمظهر" else "THEME PRESETS",
                                color = colors.ink,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "★ WESS PLUS & Gemini",
                                color = colors.magenta,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Presets chunked
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val rows = ThemeColors.Presets.chunked(3)
                            rows.forEach { rowPresets ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowPresets.forEach { preset ->
                                        val isSelected = settings.activeTheme.name == preset.name
                                        val isSig = preset.name.contains("Signature")
                                        val isGemini = preset.name.contains("Gemini")
                                        val borderCol = when {
                                            isSelected && isSig -> colors.magenta
                                            isSelected && isGemini -> Color(0xFFC58AF9)
                                            isSelected -> colors.cyan
                                            else -> colors.line
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(CyberSmallCutShape)
                                                .background(
                                                    if (isSelected) borderCol.copy(alpha = 0.22f)
                                                    else colors.void
                                                )
                                                .border(1.dp, borderCol, CyberSmallCutShape)
                                                .clickable {
                                                    onUpdateSettings(settings.copy(activeTheme = preset))
                                                }
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when {
                                                    isSig -> "★ WESS PLUS"
                                                    isGemini -> "✦ Gemini AI"
                                                    else -> preset.name
                                                },
                                                color = if (isSelected) borderCol else colors.muted,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Incognito Mode Toggle
                        SettingToggleRow(
                            label = if (isAr) "🛡️ وضع التصفح الخفي (Incognito)" else "🛡️ Private / Incognito Mode",
                            checked = settings.isIncognito,
                            onCheckedChange = { onUpdateSettings(settings.copy(isIncognito = it)) }
                        )

                        // CRT Scanlines Toggle
                        SettingToggleRow(
                            label = if (isAr) "تأثير خطوط المسح CRT" else "CRT Scanlines Effect",
                            checked = settings.scanlines,
                            onCheckedChange = { onUpdateSettings(settings.copy(scanlines = it)) }
                        )

                        // Reduce Motion Toggle
                        SettingToggleRow(
                            label = if (isAr) "تقليل الحركات والمؤثرات" else "Reduce Animations",
                            checked = settings.reducedMotion,
                            onCheckedChange = { onUpdateSettings(settings.copy(reducedMotion = it)) }
                        )

                        // Theme Darkness Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isAr) "عتامة وتباين الثيم" else "Theme Darkness",
                                    color = colors.ink,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${(settings.themeDarkness * 100).toInt()}%",
                                    color = colors.cyan,
                                    fontSize = 12.sp
                                )
                            }
                            Slider(
                                value = settings.themeDarkness,
                                onValueChange = { onUpdateSettings(settings.copy(themeDarkness = it)) },
                                valueRange = 0.1f..0.9f,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.cyan,
                                    activeTrackColor = colors.cyan,
                                    inactiveTrackColor = colors.line
                                )
                            )
                        }
                    }

                    SettingsTab.LANGUAGE -> {
                        // Dedicated Language Picker in Settings
                        Text(
                            text = if (isAr) "اختر لغة الواجهة والمتصفح" else "INTERFACE LANGUAGE",
                            color = colors.ink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        val languages = listOf(
                            Triple("ar", "العربية (الافتراضية)", "Arabic"),
                            Triple("en", "English", "الإنجليزية"),
                            Triple("fr", "Français", "الفرنسية"),
                            Triple("es", "Español", "الإسبانية"),
                            Triple("de", "Deutsch", "الألمانية")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            languages.forEach { (code, nativeName, desc) ->
                                val isSelected = settings.languageCode == code
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CyberSmallCutShape)
                                        .background(if (isSelected) colors.cyan.copy(alpha = 0.18f) else colors.void)
                                        .border(1.dp, if (isSelected) colors.cyan else colors.line, CyberSmallCutShape)
                                        .clickable { onUpdateSettings(settings.copy(languageCode = code)) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = nativeName,
                                                color = if (isSelected) colors.cyan else colors.ink,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                            Text(
                                                text = desc,
                                                color = colors.muted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = colors.cyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Speech Replies Toggle
                        SettingToggleRow(
                            label = if (isAr) "قراءة ردود الذكاء الاصطناعي بصوت مسموع" else "Speak AI Replies Aloud",
                            checked = settings.speakReplies,
                            onCheckedChange = { onUpdateSettings(settings.copy(speakReplies = it)) }
                        )

                        // AI Quality Tier
                        Text(
                            text = if (isAr) "عمق ودقة استجابة الذكاء الاصطناعي" else "AI RESPONSE QUALITY",
                            color = colors.ink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QualityTier.values().forEach { tier ->
                                val isSelected = settings.qualityTier == tier
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CyberSmallCutShape)
                                        .background(if (isSelected) colors.magenta.copy(alpha = 0.2f) else colors.void)
                                        .border(1.dp, if (isSelected) colors.magenta else colors.line, CyberSmallCutShape)
                                        .clickable { onUpdateSettings(settings.copy(qualityTier = tier)) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(tier) {
                                            QualityTier.QUICK -> if (isAr) "سريع" else "QUICK"
                                            QualityTier.BALANCED -> if (isAr) "متوازن" else "BALANCED"
                                            QualityTier.THOROUGH -> if (isAr) "مفصل" else "THOROUGH"
                                        },
                                        color = if (isSelected) colors.magenta else colors.muted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    SettingsTab.SEARCH -> {
                        Text(
                            text = if (isAr) "محرك البحث الافتراضي (Google Chrome Standard)" else "DEFAULT SEARCH ENGINE",
                            color = colors.ink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        val searchEngines = listOf(
                            Pair("Google", "https://www.google.com/search?q="),
                            Pair("DuckDuckGo", "https://duckduckgo.com/?q="),
                            Pair("Bing", "https://www.bing.com/search?q="),
                            Pair("Ecosia", "https://www.ecosia.org/search?q="),
                            Pair("WESS Quantum Search", "https://www.google.com/search?q=")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            searchEngines.forEach { (engine, _) ->
                                val isSelected = settings.searchEngine == engine
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CyberSmallCutShape)
                                        .background(if (isSelected) colors.cyan.copy(alpha = 0.18f) else colors.void)
                                        .border(1.dp, if (isSelected) colors.cyan else colors.line, CyberSmallCutShape)
                                        .clickable { onUpdateSettings(settings.copy(searchEngine = engine)) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = engine,
                                            color = if (isSelected) colors.cyan else colors.ink,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = colors.cyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsTab.PRIVACY -> {
                        Text(
                            text = if (isAr) "الخصوصية والأمان المتقدم (Chrome Security Suite)" else "PRIVACY & SECURITY SUITE",
                            color = colors.ink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        SettingToggleRow(
                            label = if (isAr) "التصفح الآمن ومكافحة البرمجيات الضارة" else "Enhanced Safe Browsing",
                            checked = settings.safeBrowsing,
                            onCheckedChange = { onUpdateSettings(settings.copy(safeBrowsing = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "حظر ملفات تعريف الارتباط للجهات الخارجية" else "Block 3rd-Party Tracking Cookies",
                            checked = settings.blockThirdPartyCookies,
                            onCheckedChange = { onUpdateSettings(settings.copy(blockThirdPartyCookies = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "فرض اتصالات HTTPS الآمنة والمشفرة" else "HTTPS-First / HTTPS-Only Mode",
                            checked = settings.httpsOnly,
                            onCheckedChange = { onUpdateSettings(settings.copy(httpsOnly = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "إرسال طلب عدم التعقب (Do Not Track)" else "Send 'Do Not Track' Header",
                            checked = settings.doNotTrack,
                            onCheckedChange = { onUpdateSettings(settings.copy(doNotTrack = it)) }
                        )
                    }

                    SettingsTab.PERFORMANCE -> {
                        Text(
                            text = if (isAr) "الأداء الفائق وإعدادات المحتوى" else "PERFORMANCE & SITE CONTENT",
                            color = colors.ink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        SettingToggleRow(
                            label = if (isAr) "موفر الذاكرة الذكي (Memory Saver)" else "Chrome Memory Saver",
                            checked = settings.memorySaver,
                            onCheckedChange = { onUpdateSettings(settings.copy(memorySaver = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "التحميل المسبق للصفحات لتصفح أسرع" else "Preload Pages for Ultra Speed",
                            checked = settings.preloadPages,
                            onCheckedChange = { onUpdateSettings(settings.copy(preloadPages = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "حظر النوافذ المنبثقة والإعلانات المزعجة" else "Block Pop-ups and Intrusive Redirects",
                            checked = settings.popupsBlocked,
                            onCheckedChange = { onUpdateSettings(settings.copy(popupsBlocked = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "محرك جافاسكريبت (JavaScript Enabled)" else "JavaScript Engine",
                            checked = settings.javascriptEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(javascriptEnabled = it)) }
                        )

                        SettingToggleRow(
                            label = if (isAr) "طلب موقع سطح المكتب افتراضياً (Desktop Site)" else "Default to Desktop Site",
                            checked = settings.desktopModeDefault,
                            onCheckedChange = { onUpdateSettings(settings.copy(desktopModeDefault = it)) }
                        )
                    }

                    SettingsTab.RESET -> {
                        Text(
                            text = if (isAr) "إدارة البيانات وإعادة الضبط" else "DATA MANAGEMENT & TOOLS",
                            color = colors.ink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        // Discord Server Planner
                        CyberButton(
                            text = if (isAr) "🎮 تخطيط خادم ديسكورد بالذكاء الاصطناعي" else "🎮 Plan a Discord Server",
                            onClick = {
                                onDismiss()
                                onTriggerDiscordPlanner()
                            },
                            isPrimary = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Clear cache & chats actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberButton(
                                text = if (isAr) "إعادة ضبط الاختصارات" else "Reset Shortcuts",
                                onClick = onResetShortcuts,
                                isPrimary = false,
                                modifier = Modifier.weight(1f)
                            )
                            CyberButton(
                                text = if (isAr) "مسح سجل المحادثات" else "Clear All Chats",
                                onClick = onClearChats,
                                isPrimary = false,
                                isMagenta = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // App Version & Chromium Engine Info
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.void.copy(alpha = 0.6f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "WESS PLUS Quantum Browser v2.5",
                                    color = colors.cyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Engine: Chromium WebKit Hybrid • AI Logic: Gemini 2.5",
                                    color = colors.muted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCyberColors.current
    Box(
        modifier = Modifier
            .clip(CyberSmallCutShape)
            .background(if (isSelected) colors.cyan.copy(alpha = 0.25f) else colors.void)
            .border(1.dp, if (isSelected) colors.cyan else colors.line, CyberSmallCutShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.cyan else colors.muted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalCyberColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.ink,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.cyan,
                checkedTrackColor = colors.cyan.copy(alpha = 0.35f),
                uncheckedThumbColor = colors.muted,
                uncheckedTrackColor = colors.panel
            )
        )
    }
}
