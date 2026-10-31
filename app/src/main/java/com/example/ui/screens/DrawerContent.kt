package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ChatThread
import com.example.model.SiteShortcut
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.components.SiteShortcutTile
import com.example.ui.theme.LocalCyberColors

@Composable
fun DrawerContent(
    threads: List<ChatThread>,
    activeThreadId: String,
    onSelectThread: (String) -> Unit,
    onNewChat: () -> Unit,
    shortcuts: List<SiteShortcut>,
    onOpenShortcut: (SiteShortcut) -> Unit,
    onDeleteShortcut: (SiteShortcut) -> Unit,
    onAddShortcut: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(colors.void)
            .border(width = 1.dp, color = colors.line)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_wess_logo),
                contentDescription = "WESS PLUS Logo",
                modifier = Modifier.size(42.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "WESS PLUS",
                color = colors.cyan,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // New Chat Button
        CyberButton(
            text = "+ New Chat",
            onClick = onNewChat,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth(),
            testTag = "drawer_new_chat_button"
        )

        // Conversations List
        Text(
            text = "CONVERSATIONS",
            color = colors.muted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (threads.isEmpty()) {
                item {
                    Text(
                        text = "No saved chats yet.",
                        color = colors.muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(threads) { thread ->
                    val isActive = thread.id == activeThreadId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CyberSmallCutShape)
                            .background(if (isActive) colors.cyan.copy(alpha = 0.12f) else colors.panel.copy(alpha = 0.6f))
                            .border(
                                width = 1.dp,
                                color = if (isActive) colors.magenta else colors.line,
                                shape = CyberSmallCutShape
                            )
                            .clickable { onSelectThread(thread.id) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isActive) colors.cyan else colors.muted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = thread.title,
                                color = if (isActive) colors.cyan else colors.ink,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Sites and Shortcuts section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHORTCUTS",
                color = colors.muted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Shortcut",
                    tint = colors.cyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(shortcuts) { sc ->
                SiteShortcutTile(
                    shortcut = sc,
                    onClick = { onOpenShortcut(sc) },
                    onDelete = { onDeleteShortcut(sc) },
                    isCompactList = true
                )
            }
        }
    }

    if (showAddDialog) {
        AddShortcutDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, url ->
                onAddShortcut(name, url)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddShortcutDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    val colors = LocalCyberColors.current
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD SHORTCUT",
                color = colors.cyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Site Name", color = colors.cyan) },
                    placeholder = { Text("e.g. GitHub", color = colors.muted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyan,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    shape = CyberSmallCutShape,
                    modifier = Modifier.fillMaxWidth().testTag("add_shortcut_name")
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Site URL", color = colors.cyan) },
                    placeholder = { Text("e.g. github.com", color = colors.muted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyan,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    shape = CyberSmallCutShape,
                    modifier = Modifier.fillMaxWidth().testTag("add_shortcut_url")
                )
            }
        },
        confirmButton = {
            CyberButton(
                text = "Add",
                onClick = {
                    if (url.isNotBlank()) {
                        val cleanName = name.ifBlank { url.substringBefore(".") }
                        onAdd(cleanName, url)
                    }
                },
                isPrimary = true
            )
        },
        dismissButton = {
            CyberButton(
                text = "Cancel",
                onClick = onDismiss,
                isPrimary = false
            )
        },
        containerColor = colors.panel,
        shape = CyberCutShape
    )
}
