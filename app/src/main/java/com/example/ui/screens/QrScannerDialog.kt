package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.theme.LocalCyberColors
import kotlin.math.abs

enum class QrTab {
    SCANNER,
    GENERATOR
}

@Composable
fun QrScannerDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onResultSelected: (String) -> Unit
) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(QrTab.SCANNER) }

    var scannedResult by remember { mutableStateOf<String?>(null) }
    var generateInput by remember { mutableStateOf(initialText.ifBlank { "https://google.com" }) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CyberCutShape)
                .background(colors.panel)
                .border(1.dp, colors.cyan, CyberCutShape)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ WESS PLUS QR MATRIX",
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

                // Tabs: Scanner vs Generator
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = colors.void,
                    contentColor = colors.cyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = colors.magenta,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == QrTab.SCANNER,
                        onClick = { selectedTab = QrTab.SCANNER },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR SCANNER", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == QrTab.GENERATOR,
                        onClick = { selectedTab = QrTab.GENERATOR },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR GENERATOR", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    )
                }

                if (selectedTab == QrTab.SCANNER) {
                    CameraScannerSection(
                        onQrDetected = { scannedResult = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Scanned Result display & Actions
                    if (scannedResult != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CyberSmallCutShape)
                                .background(colors.void)
                                .border(1.dp, colors.magenta, CyberSmallCutShape)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "TARGET DETECTED:",
                                    color = colors.magenta,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = scannedResult!!,
                                    color = colors.ink,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CyberButton(
                                        text = "Open in WESS PLUS",
                                        onClick = {
                                            onResultSelected(scannedResult!!)
                                            onDismiss()
                                        },
                                        isPrimary = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CyberButton(
                                        text = "Copy",
                                        onClick = {
                                            clipboard.setText(AnnotatedString(scannedResult!!))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        isPrimary = false,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // QR Generator Section
                    QrGeneratorSection(
                        input = generateInput,
                        onInputChange = { generateInput = it },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, generateInput)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share QR Content"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraScannerSection(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val transition = rememberInfiniteTransition(label = "laser")
    val laserY by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = modifier
            .height(230.dp)
            .clip(CyberSmallCutShape)
            .background(colors.void)
            .border(1.dp, colors.line, CyberSmallCutShape),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            // Camera preview fallback
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Camera permission needed for live scan.", color = colors.muted, fontSize = 11.sp)
                CyberButton(
                    text = "Grant Permission",
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    isPrimary = true
                )
            }
        }

        // Cyber Laser Viewfinder Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val boxSize = 160.dp.toPx()
            val left = (w - boxSize) / 2f
            val top = (h - boxSize) / 2f
            val right = left + boxSize
            val bottom = top + boxSize

            // Outer dark tint
            drawRect(color = Color.Black.copy(alpha = 0.35f))

            // Clear center frame
            drawRoundRect(
                color = colors.cyan,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Neon Scanning Laser Line
            val curY = top + boxSize * laserY
            drawLine(
                color = colors.magenta,
                start = Offset(left + 6.dp.toPx(), curY),
                end = Offset(right - 6.dp.toPx(), curY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // Quick simulation helper buttons for immediate URL scanning
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CyberSmallCutShape)
                    .background(colors.panel.copy(alpha = 0.85f))
                    .border(1.dp, colors.cyan, CyberSmallCutShape)
                    .clickable { onQrDetected("https://github.com") }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("Scan: GitHub", color = colors.cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Box(
                modifier = Modifier
                    .clip(CyberSmallCutShape)
                    .background(colors.panel.copy(alpha = 0.85f))
                    .border(1.dp, colors.magenta, CyberSmallCutShape)
                    .clickable { onQrDetected("https://ncbi.nlm.nih.gov") }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("Scan: NCBI Bio", color = colors.magenta, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun QrGeneratorSection(
    input: String,
    onInputChange: (String) -> Unit,
    onShare: () -> Unit
) {
    val colors = LocalCyberColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            label = { Text("Text or Web Link to Encode", color = colors.cyan, fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.cyan,
                unfocusedBorderColor = colors.line,
                focusedTextColor = colors.ink,
                unfocusedTextColor = colors.ink,
                cursorColor = colors.cyan
            ),
            shape = CyberSmallCutShape,
            modifier = Modifier.fillMaxWidth().testTag("qr_generator_input"),
            maxLines = 2
        )

        // Procedural Cyber QR Code Canvas
        CyberQrCanvas(
            data = input,
            primaryColor = colors.cyan,
            accentColor = colors.magenta,
            backgroundColor = colors.void
        )

        CyberButton(
            text = "Share / Transmit QR Data",
            onClick = onShare,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CyberQrCanvas(
    data: String,
    primaryColor: Color,
    accentColor: Color,
    backgroundColor: Color
) {
    val matrixSize = 25
    val seed = data.hashCode()

    Canvas(
        modifier = Modifier
            .size(170.dp)
            .clip(CyberSmallCutShape)
            .background(backgroundColor)
            .border(1.5.dp, primaryColor, CyberSmallCutShape)
            .padding(10.dp)
    ) {
        val cellSize = size.width / matrixSize

        fun isFinderPattern(r: Int, c: Int): Boolean {
            // Top-left
            if (r < 7 && c < 7) return true
            // Top-right
            if (r < 7 && c >= matrixSize - 7) return true
            // Bottom-left
            if (r >= matrixSize - 7 && c < 7) return true
            return false
        }

        fun drawFinder(r0: Int, c0: Int) {
            // Outer 7x7 box
            drawRect(
                color = primaryColor,
                topLeft = Offset(c0 * cellSize, r0 * cellSize),
                size = Size(7 * cellSize, 7 * cellSize)
            )
            // Inner 5x5 clear
            drawRect(
                color = backgroundColor,
                topLeft = Offset((c0 + 1) * cellSize, (r0 + 1) * cellSize),
                size = Size(5 * cellSize, 5 * cellSize)
            )
            // Center 3x3 solid
            drawRect(
                color = accentColor,
                topLeft = Offset((c0 + 2) * cellSize, (r0 + 2) * cellSize),
                size = Size(3 * cellSize, 3 * cellSize)
            )
        }

        // Draw Finders
        drawFinder(0, 0)
        drawFinder(0, matrixSize - 7)
        drawFinder(matrixSize - 7, 0)

        // Draw Data Modules
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (isFinderPattern(r, c)) continue

                // Timing lines
                if (r == 6 || c == 6) {
                    if ((r + c) % 2 == 0) {
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.9f, cellSize * 0.9f)
                        )
                    }
                    continue
                }

                // Deterministic module generation from data hash
                val hashBit = ((seed xor (r * 31 + c * 17)) and (1 shl ((r + c) % 16))) != 0
                if (hashBit) {
                    val modColor = if ((r + c) % 5 == 0) accentColor else primaryColor
                    drawRect(
                        color = modColor,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize * 0.88f, cellSize * 0.88f)
                    )
                }
            }
        }
    }
}
