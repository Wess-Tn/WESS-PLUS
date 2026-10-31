package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AssistantEngine
import com.example.data.PreferencesManager
import com.example.data.SpeechHelper
import com.example.model.AppMode
import com.example.model.AppSettings
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.Localization
import com.example.model.SiteShortcut
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberGridBackground
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.components.EqualizerWave
import com.example.ui.components.ScanlinesOverlay
import com.example.ui.components.VoiceOrb
import com.example.ui.theme.LocalCyberColors
import com.example.ui.theme.WessPlusTheme
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager(context) }
    val assistantEngine = remember { AssistantEngine() }

    // State
    var settings by remember { mutableStateOf(prefsManager.loadSettings()) }
    var shortcuts by remember { mutableStateOf(prefsManager.loadSites()) }
    var threads by remember {
        val loaded = prefsManager.loadChatThreads()
        mutableStateOf(
            if (loaded.isEmpty()) listOf(ChatThread(title = "New Chat")) else loaded
        )
    }
    var activeThreadId by remember { mutableStateOf(threads.first().id) }
    var currentMode by remember { mutableStateOf(AppMode.CHAT) }
    var inputQuery by remember { mutableStateOf("") }
    var addressBarText by remember { mutableStateOf("") }
    var isAddressBarFocused by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var attachedMediaUri by remember { mutableStateOf<String?>(null) }

    // Voice states
    var isVoiceModeActive by remember { mutableStateOf(false) }
    var isListeningNow by remember { mutableStateOf(false) }

    // Dialogs
    var showTranslatorDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    // Navigation Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentThread = threads.find { it.id == activeThreadId } ?: threads.first()
    val strings = Localization.get(settings.languageCode)

    fun saveAll() {
        if (!settings.isIncognito) {
            prefsManager.saveSettings(settings)
            prefsManager.saveSites(shortcuts)
            prefsManager.saveChatThreads(threads)
        }
    }

    fun openUrl(url: String) {
        try {
            var target = url.trim()
            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                target = "https://$target"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
            context.startActivity(intent)
            statusMessage = "Opened: $target"
        } catch (e: Exception) {
            statusMessage = "Could not open link: ${e.message}"
        }
    }

    // Forward declaration of handleSend so SpeechHelper can use it
    var handleSendAction: ((String) -> Unit)? by remember { mutableStateOf(null) }

    // Speech Helper
    val speechHelper = remember {
        SpeechHelper(
            context = context,
            onSpeechResult = { recognizedText ->
                inputQuery = recognizedText
                statusMessage = "Recognized: $recognizedText"
                if (isVoiceModeActive && recognizedText.isNotBlank()) {
                    handleSendAction?.invoke(recognizedText)
                }
            },
            onListeningChanged = { listening ->
                isListeningNow = listening
                if (listening) statusMessage = strings.listening + "..."
            },
            onError = { err ->
                statusMessage = err
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.destroy()
        }
    }

    // Permission launcher for Microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechHelper.startListening()
        } else {
            statusMessage = "Microphone permission is required for voice commands."
        }
    }

    // Photo picker for attachments (Play Store compliant - zero storage permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedMediaUri = uri.toString()
            statusMessage = "Media attached."
        }
    }

    // Back handler
    BackHandler(enabled = drawerState.isOpen || currentMode != AppMode.CHAT) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentMode != AppMode.CHAT) {
            currentMode = AppMode.CHAT
        }
    }

    fun handleSend(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isEmpty() && attachedMediaUri == null) return

        val userMsg = ChatMessage(
            isUser = true,
            text = trimmed.ifEmpty { "Attached Media" },
            imageUri = attachedMediaUri
        )

        // Reset input immediately
        inputQuery = ""
        val mediaToSend = attachedMediaUri
        attachedMediaUri = null

        // Add to active thread
        val updatedMessages = currentThread.messages + userMsg
        val updatedThread = currentThread.copy(
            title = if (currentThread.messages.isEmpty() && trimmed.isNotEmpty()) trimmed.take(24) else currentThread.title,
            messages = updatedMessages
        )
        threads = threads.map { if (it.id == activeThreadId) updatedThread else it }
        isThinking = true
        statusMessage = "Processing neural query..."

        coroutineScope.launch {
            val response = assistantEngine.processQuery(
                query = trimmed,
                shortcuts = shortcuts,
                tier = settings.qualityTier,
                lang = settings.languageCode
            )

            // If assistant produced a new shortcut
            if (response.shortcutToAdd != null) {
                val (name, url) = response.shortcutToAdd
                val mark = name.firstOrNull()?.uppercase() ?: "•"
                val colorsList = listOf("#00F0FF", "#FF2A6D", "#FCEE0A", "#7CFF6B", "#B38CFF")
                val colorHex = colorsList[shortcuts.size % colorsList.size]
                shortcuts = shortcuts + SiteShortcut(name = name, url = url, mark = mark, colorHex = colorHex)
            }

            val botMsg = ChatMessage(
                isUser = false,
                text = response.text,
                links = response.links
            )

            threads = threads.map { thread ->
                if (thread.id == activeThreadId) {
                    thread.copy(messages = thread.messages + botMsg)
                } else thread
            }
            isThinking = false
            statusMessage = "Ready."
            saveAll()

            // Speak response if voice mode or speakReplies is on
            if (isVoiceModeActive || settings.speakReplies) {
                speechHelper.speak(response.text, settings.languageCode)
            }
        }
    }

    // Keep handleSendAction updated
    LaunchedEffect(currentThread, activeThreadId, settings, shortcuts) {
        handleSendAction = { text -> handleSend(text) }
    }

    WessPlusTheme(themeColors = settings.activeTheme) {
        val colors = LocalCyberColors.current

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = colors.void,
                    modifier = Modifier.width(280.dp)
                ) {
                    DrawerContent(
                        threads = threads,
                        activeThreadId = activeThreadId,
                        onSelectThread = { id ->
                            activeThreadId = id
                            currentMode = AppMode.CHAT
                            coroutineScope.launch { drawerState.close() }
                        },
                        onNewChat = {
                            val newT = ChatThread(title = strings.newChat)
                            threads = listOf(newT) + threads
                            activeThreadId = newT.id
                            currentMode = AppMode.CHAT
                            coroutineScope.launch { drawerState.close() }
                            saveAll()
                        },
                        shortcuts = shortcuts,
                        onOpenShortcut = { sc ->
                            openUrl(sc.url)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onDeleteShortcut = { sc ->
                            shortcuts = shortcuts.filter { it.id != sc.id }
                            saveAll()
                        },
                        onAddShortcut = { name, url ->
                            val mark = name.firstOrNull()?.uppercase() ?: "•"
                            val colorsList = listOf("#00F0FF", "#FF2A6D", "#FCEE0A", "#7CFF6B", "#B38CFF")
                            val colorHex = colorsList[shortcuts.size % colorsList.size]
                            shortcuts = shortcuts + SiteShortcut(name = name, url = url, mark = mark, colorHex = colorHex)
                            saveAll()
                        }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.void)
            ) {
                // Signature Cyber Grid & Glow
                CyberGridBackground(enabled = true)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        // 1. TOP HEADER & OMNI-ADDRESS BAR
                        // 1. TOP HEADER & OMNI-ADDRESS BAR (Language moved to Settings)
                        TopCyberHeader(
                            onMenuClick = { coroutineScope.launch { drawerState.open() } },
                            addressText = addressBarText,
                            onAddressChange = { addressBarText = it },
                            onAddressSubmit = {
                                val q = addressBarText.trim()
                                if (q.isNotEmpty()) {
                                    if (q.contains(".") && !q.contains(" ")) {
                                        openUrl(q)
                                    } else {
                                        val searchBase = when (settings.searchEngine) {
                                            "DuckDuckGo" -> "https://duckduckgo.com/?q="
                                            "Bing" -> "https://www.bing.com/search?q="
                                            "Ecosia" -> "https://www.ecosia.org/search?q="
                                            else -> "https://www.google.com/search?q="
                                        }
                                        openUrl(searchBase + URLEncoder.encode(q, "UTF-8"))
                                    }
                                    addressBarText = ""
                                    isAddressBarFocused = false
                                }
                            },
                            onFocusChange = { isAddressBarFocused = it },
                            onOpenQrScanner = { showQrDialog = true },
                            onOpenTranslator = { showTranslatorDialog = true },
                            onOpenSettings = { showSettingsDialog = true },
                            searchPlaceholder = strings.searchOrUrl
                        )

                        // Suggestion Dropdown if address bar has query
                        if (isAddressBarFocused && addressBarText.isNotBlank()) {
                            AddressSuggestionDropdown(
                                query = addressBarText,
                                shortcuts = shortcuts,
                                onOpenUrl = { url ->
                                    openUrl(url)
                                    addressBarText = ""
                                    isAddressBarFocused = false
                                },
                                onAskAI = { query ->
                                    currentMode = AppMode.CHAT
                                    addressBarText = ""
                                    isAddressBarFocused = false
                                    handleSend(query)
                                }
                            )
                        }

                        // 2. SUB-BAR: MODE TOGGLES (CHAT, IMAGE, BIOSCIENCE, AI_TOOLS)
                        SubModeBar(
                            currentMode = currentMode,
                            onSelectMode = { currentMode = it },
                            strings = strings
                        )

                        // 4. MAIN TAB CONTENT AREA
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (currentMode) {
                                AppMode.CHAT -> {
                                    ChatTabScreen(
                                        messages = currentThread.messages,
                                        shortcuts = shortcuts,
                                        onOpenShortcut = { openUrl(it.url) },
                                        onDeleteShortcut = { sc ->
                                            shortcuts = shortcuts.filter { it.id != sc.id }
                                            saveAll()
                                        },
                                        onAddShortcutClick = { showAddShortcutDialog = true },
                                        onSendPrompt = { handleSend(it) },
                                        isThinking = isThinking
                                    )
                                }
                                AppMode.IMAGE -> {
                                    ImageGenTabScreen(
                                        onSendQuery = { prompt ->
                                            handleSend("Generate image: $prompt")
                                        }
                                    )
                                }
                                AppMode.BIOSCIENCE -> {
                                    BioScienceScreen(
                                        onSendQueryToChat = { query ->
                                            currentMode = AppMode.CHAT
                                            handleSend(query)
                                        }
                                    )
                                }
                                AppMode.AI_TOOLS -> {
                                    AiToolsScreen(
                                        currentLang = settings.languageCode,
                                        onSendQueryToChat = { query ->
                                            currentMode = AppMode.CHAT
                                            handleSend(query)
                                        }
                                    )
                                }
                            }
                        }

                        // 3. COMPOSER BAR (Available in Chat & Image modes)
                        if (currentMode == AppMode.CHAT || currentMode == AppMode.IMAGE) {
                            CyberComposerBar(
                                query = inputQuery,
                                onQueryChange = { inputQuery = it },
                                onSend = { handleSend(inputQuery) },
                                isListening = isListeningNow,
                                isVoiceMode = isVoiceModeActive,
                                onToggleVoice = {
                                    speechHelper.playPromptChime()
                                    isVoiceModeActive = !isVoiceModeActive
                                    if (isVoiceModeActive) {
                                        statusMessage = strings.voiceOn
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            speechHelper.startListening()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    } else {
                                        speechHelper.stopListening()
                                        speechHelper.stopSpeaking()
                                        statusMessage = "Voice off."
                                    }
                                },
                                onMicClick = {
                                    speechHelper.playPromptChime()
                                    if (isListeningNow) {
                                        speechHelper.stopListening()
                                    } else {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            speechHelper.startListening()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                },
                                onAttachClick = {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                },
                                hasAttachment = attachedMediaUri != null,
                                mode = currentMode,
                                status = statusMessage,
                                placeholder = if (currentMode == AppMode.IMAGE) strings.imagePlaceholder else strings.askPlaceholder
                            )
                        }
                    }
                }

                // CRT Scanlines Overlay
                ScanlinesOverlay(enabled = settings.scanlines)
            }
        }

        // Dialogs
        if (showQrDialog) {
            QrScannerDialog(
                initialText = addressBarText,
                onDismiss = { showQrDialog = false },
                onResultSelected = { result ->
                    if (result.contains(".") && !result.contains(" ")) {
                        openUrl(result)
                    } else {
                        currentMode = AppMode.CHAT
                        handleSend(result)
                    }
                    showQrDialog = false
                }
            )
        }

        if (showTranslatorDialog) {
            TranslatorDialog(
                initialText = inputQuery,
                currentUrl = addressBarText,
                currentAppLang = settings.languageCode,
                onDismiss = { showTranslatorDialog = false },
                onSpeak = { text, lang -> speechHelper.speak(text, lang) }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                settings = settings,
                onUpdateSettings = {
                    settings = it
                    saveAll()
                },
                onTriggerDiscordPlanner = {
                    currentMode = AppMode.CHAT
                    handleSend("Plan a Discord server for my project: list categories, channels, roles, and a welcome message.")
                },
                onResetShortcuts = {
                    shortcuts = PreferencesManager.DEFAULT_SITES
                    saveAll()
                    statusMessage = strings.resetShortcuts
                },
                onClearChats = {
                    threads = listOf(ChatThread(title = strings.newChat))
                    activeThreadId = threads.first().id
                    saveAll()
                    statusMessage = strings.clearChats
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showAddShortcutDialog) {
            AddShortcutDialog(
                onDismiss = { showAddShortcutDialog = false },
                onAdd = { name, url ->
                    val mark = name.firstOrNull()?.uppercase() ?: "•"
                    val colorsList = listOf("#00F0FF", "#FF2A6D", "#FCEE0A", "#7CFF6B", "#B38CFF")
                    val colorHex = colorsList[shortcuts.size % colorsList.size]
                    shortcuts = shortcuts + SiteShortcut(name = name, url = url, mark = mark, colorHex = colorHex)
                    saveAll()
                    showAddShortcutDialog = false
                    statusMessage = "Shortcut added."
                }
            )
        }
    }
}

// -------------------------------------------------------------
// Top Cyber Header with Address Bar & QR Scanner
// -------------------------------------------------------------
@Composable
private fun TopCyberHeader(
    onMenuClick: () -> Unit,
    addressText: String,
    onAddressChange: (String) -> Unit,
    onAddressSubmit: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenTranslator: () -> Unit,
    onOpenSettings: () -> Unit,
    searchPlaceholder: String
) {
    val colors = LocalCyberColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panel.copy(alpha = 0.95f))
            .border(1.dp, colors.line)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Drawer Menu Button
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CyberSmallCutShape)
                .border(1.dp, colors.line, CyberSmallCutShape)
                .testTag("menu_drawer_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = colors.cyan,
                modifier = Modifier.size(20.dp)
            )
        }

        // Omni-Address Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(CyberCutShape)
                .background(colors.void.copy(alpha = 0.85f))
                .border(1.dp, colors.cyan.copy(alpha = 0.6f), CyberCutShape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.muted,
                    modifier = Modifier.size(16.dp)
                )
                BasicTextField(
                    value = addressText,
                    onValueChange = onAddressChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { onFocusChange(it.isFocused) }
                        .testTag("omni_address_input"),
                    textStyle = TextStyle(
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.cyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onAddressSubmit() }),
                    decorationBox = { innerTextField ->
                        if (addressText.isEmpty()) {
                            Text(
                                text = searchPlaceholder,
                                color = colors.muted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )

                // QR Scanner Button inside address bar
                IconButton(
                    onClick = onOpenQrScanner,
                    modifier = Modifier.size(26.dp).testTag("qr_scanner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = colors.cyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Translator Button
        IconButton(
            onClick = onOpenTranslator,
            modifier = Modifier
                .size(36.dp)
                .clip(CyberSmallCutShape)
                .border(1.dp, colors.line, CyberSmallCutShape)
                .testTag("translator_button")
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Translator",
                tint = colors.cyan,
                modifier = Modifier.size(18.dp)
            )
        }

        // Settings Button (Language, Themes & Chrome configurations)
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(36.dp)
                .clip(CyberSmallCutShape)
                .border(1.dp, colors.line, CyberSmallCutShape)
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = colors.cyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Suggestion Dropdown
// -------------------------------------------------------------
@Composable
private fun AddressSuggestionDropdown(
    query: String,
    shortcuts: List<SiteShortcut>,
    onOpenUrl: (String) -> Unit,
    onAskAI: (String) -> Unit
) {
    val colors = LocalCyberColors.current
    val qLower = query.lowercase().trim()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CyberCutShape)
            .background(colors.panel)
            .border(1.dp, colors.cyan, CyberCutShape)
            .padding(6.dp)
    ) {
        shortcuts.filter { it.name.lowercase().contains(qLower) || it.url.lowercase().contains(qLower) }
            .take(3)
            .forEach { sc ->
                SuggestionItem(
                    label = "↗ Open ${sc.name} (${sc.url})",
                    onClick = { onOpenUrl(sc.url) }
                )
            }

        SuggestionItem(
            label = "🔍 Search for «$query»",
            onClick = { onOpenUrl("https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")) }
        )

        SuggestionItem(
            label = "🤖 Ask WESS PLUS «$query»",
            onClick = { onAskAI(query) }
        )
    }
}

@Composable
private fun SuggestionItem(label: String, onClick: () -> Unit) {
    val colors = LocalCyberColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = colors.ink,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// Sub-Bar: 4 Modes (CHAT, IMAGE, BIOSCIENCE, AI_TOOLS)
// -------------------------------------------------------------
@Composable
private fun SubModeBar(
    currentMode: AppMode,
    onSelectMode: (AppMode) -> Unit,
    strings: com.example.model.StringsDict
) {
    val colors = LocalCyberColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.void)
            .border(width = 0.5.dp, color = colors.line)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                ModeChip(
                    label = strings.chatMode,
                    isSelected = currentMode == AppMode.CHAT,
                    onClick = { onSelectMode(AppMode.CHAT) }
                )
            }
            item {
                ModeChip(
                    label = strings.imageMode,
                    isSelected = currentMode == AppMode.IMAGE,
                    onClick = { onSelectMode(AppMode.IMAGE) }
                )
            }
            item {
                ModeChip(
                    label = strings.bioMode,
                    isSelected = currentMode == AppMode.BIOSCIENCE,
                    onClick = { onSelectMode(AppMode.BIOSCIENCE) },
                    isHighlight = true
                )
            }
            item {
                ModeChip(
                    label = strings.aiToolsMode,
                    isSelected = currentMode == AppMode.AI_TOOLS,
                    onClick = { onSelectMode(AppMode.AI_TOOLS) },
                    isMagenta = true
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isHighlight: Boolean = false,
    isMagenta: Boolean = false
) {
    val colors = LocalCyberColors.current
    val activeBorder = if (isMagenta) colors.magenta else if (isHighlight) colors.cyan else colors.cyan

    Box(
        modifier = Modifier
            .testTag("mode_chip_$label")
            .clip(CyberSmallCutShape)
            .background(
                if (isSelected) (if (isMagenta) colors.magenta else colors.cyan).copy(alpha = 0.25f)
                else colors.panel
            )
            .border(
                width = 1.dp,
                color = if (isSelected) activeBorder else colors.line,
                shape = CyberSmallCutShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) (if (isMagenta) colors.magenta else colors.cyan) else colors.muted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// -------------------------------------------------------------
// Composer Bar with Voice Orb in Native Bottom Position
// -------------------------------------------------------------
@Composable
private fun CyberComposerBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    isVoiceMode: Boolean,
    onToggleVoice: () -> Unit,
    onMicClick: () -> Unit,
    onAttachClick: () -> Unit,
    hasAttachment: Boolean,
    mode: AppMode,
    status: String,
    placeholder: String
) {
    val colors = LocalCyberColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Voice Mode Status Strip (if voice mode active or listening)
        if (isVoiceMode || isListening) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isListening) colors.magenta else colors.cyan)
                    )
                    Text(
                        text = if (isListening) "LISTENING TO VOICE..." else "VOICE MODE ACTIVE (SPEAK FREELY)",
                        color = if (isListening) colors.magenta else colors.cyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                EqualizerWave(color = if (isListening) colors.magenta else colors.cyan)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CyberCutShape)
                .background(colors.panel.copy(alpha = 0.95f))
                .border(1.dp, if (isVoiceMode) colors.magenta else colors.line, CyberCutShape)
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Voice Orb in native bottom position
                VoiceOrb(
                    isLive = isListening,
                    isVoiceMode = isVoiceMode,
                    onClick = onToggleVoice
                )

                // Media Attachment button
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CyberSmallCutShape)
                        .background(if (hasAttachment) colors.cyan.copy(alpha = 0.25f) else Color.Transparent)
                        .border(1.dp, colors.cyan, CyberSmallCutShape)
                        .testTag("composer_attach_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Media",
                        tint = colors.cyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Text Input
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("composer_text_input"),
                    textStyle = TextStyle(
                        color = colors.ink,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Default,
                        lineHeight = 18.sp
                    ),
                    maxLines = 4,
                    cursorBrush = SolidColor(colors.cyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = colors.muted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )

                // Send button
                IconButton(
                    onClick = onSend,
                    enabled = query.isNotBlank() || hasAttachment,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CyberSmallCutShape)
                        .background(if (query.isNotBlank() || hasAttachment) colors.cyan else colors.cyan.copy(alpha = 0.3f))
                        .testTag("composer_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = colors.void,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live status message
        if (status.isNotBlank()) {
            Text(
                text = status,
                color = colors.cyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, top = 3.dp)
            )
        }
    }
}
