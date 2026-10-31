package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ChatMessage
import com.example.model.SiteShortcut
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.components.LinkChip
import com.example.ui.components.SiteShortcutTile
import com.example.ui.components.ThinkingBars
import com.example.ui.theme.LocalCyberColors

@Composable
fun ChatTabScreen(
    messages: List<ChatMessage>,
    shortcuts: List<SiteShortcut>,
    onOpenShortcut: (SiteShortcut) -> Unit,
    onDeleteShortcut: (SiteShortcut) -> Unit,
    onAddShortcutClick: () -> Unit,
    onSendPrompt: (String) -> Unit,
    isThinking: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        // Hero Landing Page (matching WESS PLUS Web)
        HeroLandingPage(
            shortcuts = shortcuts,
            onOpenShortcut = onOpenShortcut,
            onDeleteShortcut = onDeleteShortcut,
            onAddShortcutClick = onAddShortcutClick,
            onSendPrompt = onSendPrompt,
            modifier = modifier
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onOpenLink = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }

            if (isThinking) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        ThinkingBars()
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HeroLandingPage(
    shortcuts: List<SiteShortcut>,
    onOpenShortcut: (SiteShortcut) -> Unit,
    onDeleteShortcut: (SiteShortcut) -> Unit,
    onAddShortcutClick: () -> Unit,
    onSendPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current

    val quickPrompts = listOf(
        "🧬 Analyze GFP DNA sequence and predict translation",
        "✂️ Find CRISPR-Cas9 targets for gene knockout",
        "🧪 Calculate 50 µL PCR Master Mix for 20 reactions",
        "🎮 Plan a Discord server for our bioinformatics lab"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Official WESS PLUS Cyber Shield Logo (Clean, free-floating without box/border)
            Image(
                painter = painterResource(id = R.drawable.ic_wess_logo),
                contentDescription = "WESS PLUS",
                modifier = Modifier
                    .size(110.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HELLO, I'M WESS PLUS",
                    color = colors.cyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Open a site from your shortcuts, ask a question, analyze bioscience sequences, or generate an image.",
                    color = colors.muted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontFamily = FontFamily.Default
                )
            }
        }

        // Shortcuts Grid Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SITES & SHORTCUTS",
                        color = colors.ink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ Add Shortcut",
                        color = colors.cyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable(onClick = onAddShortcutClick)
                    )
                }

                // Grid of shortcuts (2 per row)
                val chunked = shortcuts.chunked(2)
                chunked.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { shortcut ->
                            SiteShortcutTile(
                                shortcut = shortcut,
                                onClick = { onOpenShortcut(shortcut) },
                                onDelete = { onDeleteShortcut(shortcut) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Quick Exploration Chips
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EXPLORE WITH WESS PLUS",
                    color = colors.ink,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                quickPrompts.forEach { qp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CyberSmallCutShape)
                            .background(colors.panel.copy(alpha = 0.85f))
                            .border(1.dp, colors.line, CyberSmallCutShape)
                            .clickable { onSendPrompt(qp) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = qp,
                            color = colors.cyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onOpenLink: (String) -> Unit
) {
    val colors = LocalCyberColors.current
    val isUser = message.isUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_row" else "assistant_message_row"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.88f else 1f)
                .clip(CyberCutShape)
                .background(
                    if (isUser) colors.magenta.copy(alpha = 0.12f)
                    else colors.panel.copy(alpha = 0.95f)
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) colors.magenta.copy(alpha = 0.45f) else colors.cyan.copy(alpha = 0.45f),
                    shape = CyberCutShape
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header (Assistant / User label)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "USER" else "WESS PLUS",
                        color = if (isUser) colors.magenta else colors.cyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Media Attachment if any
                if (message.imageUri != null) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "Attached Media",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(CyberSmallCutShape)
                            .border(1.dp, colors.line, CyberSmallCutShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Text
                Text(
                    text = message.text,
                    color = colors.ink,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Default
                )

                // Interactive Links
                if (message.links.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        message.links.forEach { link ->
                            LinkChip(
                                text = link.title,
                                onClick = { onOpenLink(link.url) }
                            )
                        }
                    }
                }
            }
        }
    }
}
