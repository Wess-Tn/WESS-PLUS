package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SiteShortcut
import com.example.ui.theme.LocalCyberColors
import com.example.ui.theme.parseHexColor

val CyberCutShape = CutCornerShape(10.dp)
val CyberSmallCutShape = CutCornerShape(6.dp)

@Composable
fun ScanlinesOverlay(modifier: Modifier = Modifier, enabled: Boolean = true) {
    if (!enabled) return
    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 4.dp.toPx()
        val linePaint = Color.Black.copy(alpha = 0.16f)
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = linePaint,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += step
        }
    }
}

@Composable
fun CyberGridBackground(modifier: Modifier = Modifier, enabled: Boolean = true) {
    if (!enabled) return
    val colors = LocalCyberColors.current
    val transition = rememberInfiniteTransition(label = "grid_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val step = 32.dp.toPx()

        var x = 0f
        while (x < w) {
            drawLine(
                color = colors.cyan.copy(alpha = pulse),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < h) {
            drawLine(
                color = colors.cyan.copy(alpha = pulse),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += step
        }

        // Corner futuristic gradient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.magenta.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(w * 0.85f, 0f),
                radius = 280.dp.toPx()
            ),
            radius = 280.dp.toPx(),
            center = Offset(w * 0.85f, 0f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.cyan.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(0f, h),
                radius = 260.dp.toPx()
            ),
            radius = 260.dp.toPx(),
            center = Offset(0f, h)
        )
    }
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isMagenta: Boolean = false,
    testTag: String = "cyber_button"
) {
    val colors = LocalCyberColors.current
    val accent = if (isMagenta) colors.magenta else colors.cyan

    val backgroundBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                accent.copy(alpha = 0.25f),
                if (isMagenta) colors.cyan.copy(alpha = 0.25f) else colors.magenta.copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(colors.panel.copy(alpha = 0.85f), colors.panel.copy(alpha = 0.85f))
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(CyberCutShape)
            .background(backgroundBrush)
            .border(1.dp, accent, CyberCutShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accent),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPrimary) colors.ink else accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun VoiceOrb(
    isLive: Boolean,
    isVoiceMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    val activeColor = if (isLive) colors.magenta else colors.cyan

    val transition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .testTag("voice_orb_pill")
            .clip(CircleShape)
            .background(if (isLive) colors.magenta.copy(alpha = 0.18f) else colors.cyan.copy(alpha = 0.08f))
            .border(1.dp, activeColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Glowing Orb
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(28.dp * pulseScale)
                        .clip(CircleShape)
                        .background(colors.magenta.copy(alpha = 0.25f))
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.9f),
                                activeColor,
                                colors.void
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = colors.void,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        if (isLive) {
            EqualizerWave(color = colors.magenta)
        }

        Text(
            text = if (isLive) "LISTENING" else if (isVoiceMode) "VOICE ON" else "VOICE",
            color = activeColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun EqualizerWave(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "eq_bars")
    val bar1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(bar1, bar2, bar3).forEach { scale ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((14 * scale).dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun ThinkingBars(modifier: Modifier = Modifier) {
    val colors = LocalCyberColors.current
    val transition = rememberInfiniteTransition(label = "think")
    val anim1 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "t1"
    )
    val anim2 by transition.animateFloat(
        initialValue = 0.7f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "t2"
    )
    val anim3 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "t3"
    )

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(anim1, anim2, anim3).forEach { heightRatio ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((16 * heightRatio).dp)
                    .background(colors.cyan)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "PROCESSING...",
            color = colors.cyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LinkChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    Box(
        modifier = modifier
            .testTag("link_chip_$text")
            .clip(CyberSmallCutShape)
            .background(colors.cyan.copy(alpha = 0.10f))
            .border(1.dp, colors.cyan, CyberSmallCutShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "↗ $text",
            color = colors.cyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SiteShortcutTile(
    shortcut: SiteShortcut,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isCompactList: Boolean = false
) {
    val colors = LocalCyberColors.current
    val markColor = parseHexColor(shortcut.colorHex, colors.cyan)

    Box(
        modifier = modifier
            .testTag("shortcut_tile_${shortcut.name}")
            .clip(CyberSmallCutShape)
            .background(colors.panel.copy(alpha = 0.92f))
            .border(1.dp, colors.line, CyberSmallCutShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mark Badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .border(1.dp, markColor, CyberSmallCutShape)
                    .background(markColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shortcut.mark,
                    color = markColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = shortcut.name,
                color = colors.ink,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove shortcut",
                    tint = colors.muted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
