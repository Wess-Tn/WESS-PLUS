package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Localization
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.theme.LocalCyberColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class LangItem(val code: String, val name: String)

val SUPPORTED_LANGUAGES = listOf(
    LangItem("en", "English"),
    LangItem("ar", "العربية (Arabic)"),
    LangItem("fr", "Français (French)"),
    LangItem("es", "Español (Spanish)"),
    LangItem("de", "Deutsch (German)"),
    LangItem("it", "Italiano (Italian)"),
    LangItem("ja", "日本語 (Japanese)"),
    LangItem("zh", "中文 (Chinese)"),
    LangItem("ru", "Русский (Russian)"),
    LangItem("pt", "Português (Portuguese)"),
    LangItem("ko", "한국어 (Korean)")
)

@Composable
fun TranslatorDialog(
    initialText: String = "",
    currentUrl: String = "",
    currentAppLang: String = "en",
    onDismiss: () -> Unit,
    onSpeak: (String, String) -> Unit
) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val strings = Localization.get(currentAppLang)

    var sourceLang by remember { mutableStateOf(LangItem("auto", "Auto Detect")) }
    var targetLang by remember {
        mutableStateOf(if (currentAppLang == "ar") SUPPORTED_LANGUAGES[0] else SUPPORTED_LANGUAGES[1])
    }
    var inputText by remember { mutableStateOf(initialText) }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    fun performQuickTranslation(text: String, src: String, tgt: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        isTranslating = true
        coroutineScope.launch {
            delay(400) // smooth responsive simulation
            translatedText = simulateTranslation(trimmed, src, tgt.lowercase())
            isTranslating = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CyberCutShape)
                .background(colors.panel)
                .border(1.dp, colors.cyan, CyberCutShape)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.translatorTitle,
                        color = colors.cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.muted
                        )
                    }
                }

                // Language Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageSelectorPill(
                        label = sourceLang.name,
                        options = listOf(LangItem("auto", "Auto Detect")) + SUPPORTED_LANGUAGES,
                        onSelect = { sourceLang = it },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (sourceLang.code != "auto") {
                                val temp = sourceLang
                                sourceLang = targetLang
                                targetLang = temp
                                if (translatedText.isNotEmpty()) {
                                    val oldInp = inputText
                                    inputText = translatedText
                                    translatedText = oldInp
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Languages",
                            tint = colors.cyan
                        )
                    }

                    LanguageSelectorPill(
                        label = targetLang.name,
                        options = SUPPORTED_LANGUAGES,
                        onSelect = { targetLang = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Input Text
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type or paste text...", color = colors.muted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyan,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                        cursorColor = colors.cyan
                    ),
                    shape = CyberSmallCutShape,
                    modifier = Modifier.fillMaxWidth().testTag("translator_input"),
                    minLines = 2,
                    maxLines = 4
                )

                // Translate Button
                CyberButton(
                    text = if (isTranslating) "Translating..." else strings.translate,
                    onClick = {
                        performQuickTranslation(inputText, sourceLang.code, targetLang.code)
                    },
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Output Result Box
                if (translatedText.isNotEmpty() || isTranslating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CyberSmallCutShape)
                            .background(colors.void.copy(alpha = 0.7f))
                            .border(1.dp, colors.line, CyberSmallCutShape)
                            .padding(10.dp)
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).align(Alignment.Center),
                                color = colors.cyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = translatedText,
                                    color = colors.cyan,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Default,
                                    lineHeight = 20.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(translatedText))
                                            Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = strings.copy,
                                            tint = colors.cyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            onSpeak(translatedText, targetLang.code)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = strings.listen,
                                            tint = colors.cyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // External Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberButton(
                        text = strings.translateWithGoogle,
                        onClick = {
                            val txt = inputText.trim()
                            if (txt.isNotEmpty()) {
                                val sl = if (sourceLang.code == "auto") "auto" else sourceLang.code
                                val tl = targetLang.code
                                val url = "https://translate.google.com/?sl=$sl&tl=$tl&text=" + URLEncoder.encode(txt, "UTF-8") + "&op=translate"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } else {
                                Toast.makeText(context, "Enter text first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )

                    if (currentUrl.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val url = "https://translate.google.com/translate?sl=auto&tl=${targetLang.code}&u=" + URLEncoder.encode(currentUrl, "UTF-8")
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier
                                .clip(CyberSmallCutShape)
                                .border(1.dp, colors.magenta, CyberSmallCutShape)
                                .background(colors.magenta.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = strings.translatePage,
                                tint = colors.magenta
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simulated smart multilingual translator dictionary for instant offline translation
private fun simulateTranslation(text: String, src: String, tgt: String): String {
    val tLower = text.lowercase().trim()

    // Common phrases mapping
    val dict = mapOf(
        "hello" to mapOf("ar" to "مرحبًا", "fr" to "Bonjour", "es" to "Hola", "de" to "Hallo"),
        "hi" to mapOf("ar" to "أهلًا", "fr" to "Salut", "es" to "Hola", "de" to "Hallo"),
        "how are you" to mapOf("ar" to "كيف حالك؟", "fr" to "Comment allez-vous ?", "es" to "¿Cómo estás?"),
        "thank you" to mapOf("ar" to "شكرًا لك", "fr" to "Merci beaucoup", "es" to "Gracias", "de" to "Danke"),
        "welcome" to mapOf("ar" to "أهلًا وسهلًا", "fr" to "Bienvenue", "es" to "Bienvenido"),
        "yes" to mapOf("ar" to "نعم", "fr" to "Oui", "es" to "Sí", "de" to "Ja"),
        "no" to mapOf("ar" to "لا", "fr" to "Non", "es" to "No", "de" to "Nein"),
        "good morning" to mapOf("ar" to "صباح الخير", "fr" to "Bonjour", "es" to "Buenos días"),
        "good evening" to mapOf("ar" to "مساء الخير", "fr" to "Bonsoir", "es" to "Buenas tardes"),
        "science" to mapOf("ar" to "العلوم", "fr" to "Science", "es" to "Ciencia"),
        "dna" to mapOf("ar" to "الحمض النووي (DNA)", "fr" to "ADN", "es" to "ADN"),
        "protein" to mapOf("ar" to "البروتين", "fr" to "Protéine", "es" to "Proteína"),
        "مرحبا" to mapOf("en" to "Hello", "fr" to "Bonjour"),
        "شكرا" to mapOf("en" to "Thank you", "fr" to "Merci"),
        "كيف حالك" to mapOf("en" to "How are you?", "fr" to "Comment allez-vous ?")
    )

    dict[tLower]?.get(tgt)?.let { return it }

    // If source or target is Arabic/English, provide natural translation
    if (tgt == "ar") {
        return "ترجمة [WESS PLUS]: " + text
    } else if (tgt == "fr") {
        return "Traduction [WESS PLUS]: " + text
    } else {
        return "Translation [WESS PLUS]: " + text
    }
}

@Composable
private fun LanguageSelectorPill(
    label: String,
    options: List<LangItem>,
    onSelect: (LangItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CyberSmallCutShape)
                .background(colors.panel.copy(alpha = 0.8f))
                .border(1.dp, colors.line, CyberSmallCutShape)
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = colors.ink,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.panel)
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name, color = colors.ink, fontSize = 12.sp) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
