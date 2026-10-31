package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bioscience.BioScienceEngine
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCutShape
import com.example.ui.components.CyberSmallCutShape
import com.example.ui.theme.LocalCyberColors

enum class BioSubTab(val label: String) {
    DNA_TRANSLATION("DNA / RNA"),
    PROTEIN_INSPECTOR("PROTEIN / PI"),
    CRISPR_GUIDE("CRISPR-CAS9"),
    LAB_CALCULATOR("LAB MATH"),
    PROTOCOLS("PROTOCOLS")
}

@Composable
fun BioScienceScreen(
    onSendQueryToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    var selectedTab by remember { mutableStateOf(BioSubTab.DNA_TRANSLATION) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.void)
    ) {
        // High-tech sub-navigation tab row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = colors.panel,
            contentColor = colors.cyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = colors.magenta,
                    height = 2.dp
                )
            }
        ) {
            BioSubTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) colors.cyan else colors.muted
                        )
                    }
                )
            }
        }

        // Sub-screen content
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
            when (selectedTab) {
                BioSubTab.DNA_TRANSLATION -> DnaAnalyzerSection(onSendQueryToChat)
                BioSubTab.PROTEIN_INSPECTOR -> ProteinInspectorSection(onSendQueryToChat)
                BioSubTab.CRISPR_GUIDE -> CrisprFinderSection(onSendQueryToChat)
                BioSubTab.LAB_CALCULATOR -> LabCalculatorSection()
                BioSubTab.PROTOCOLS -> BioProtocolsSection(onSendQueryToChat)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. DNA / RNA Translation Section
// -------------------------------------------------------------
@Composable
private fun DnaAnalyzerSection(onSendQueryToChat: (String) -> Unit) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var sequenceInput by remember { mutableStateOf(BioScienceEngine.SampleDnaGfp) }
    val analysis = remember(sequenceInput) { BioScienceEngine.analyzeDna(sequenceInput) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🧬 MOLECULAR SEQUENCE TRANSLATOR & STATS",
                color = colors.cyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter single or double stranded nucleotide sequence. Calculates GC%, Tm, MW, reverse complement, and 6-frame protein translation.",
                color = colors.muted,
                fontSize = 12.sp
            )
        }

        // Preset buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CyberButton(
                    text = "Preset: eGFP",
                    onClick = { sequenceInput = BioScienceEngine.SampleDnaGfp },
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                CyberButton(
                    text = "Preset: Insulin",
                    onClick = { sequenceInput = BioScienceEngine.SampleDnaInsulin },
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Input text field
        item {
            OutlinedTextField(
                value = sequenceInput,
                onValueChange = { sequenceInput = it },
                label = { Text("DNA Nucleotide Sequence (5'->3')", color = colors.cyan) },
                placeholder = { Text("e.g. ATGGTGAGCAAG...", color = colors.muted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.cyan,
                    unfocusedBorderColor = colors.line,
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink,
                    cursorColor = colors.cyan
                ),
                shape = CyberCutShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dna_sequence_input"),
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
        }

        // Stats grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Length", "${analysis.length} bp", colors.cyan, Modifier.weight(1f))
                StatCard("GC Content", "${analysis.gcPercent}%", colors.magenta, Modifier.weight(1f))
                StatCard("Melting (Tm)", "${analysis.meltingTempTm} °C", colors.ink, Modifier.weight(1f))
                StatCard("Mol. Wt.", "${analysis.molecularWeightKDa} kDa", colors.cyan, Modifier.weight(1f))
            }
        }

        // Reverse Complement Card
        item {
            BioOutputCard(
                title = "Reverse Complement (5'->3')",
                content = analysis.reverseComplement,
                onCopy = {
                    clipboard.setText(AnnotatedString(analysis.reverseComplement))
                    Toast.makeText(context, "Copied Reverse Complement", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // RNA Transcription Card
        item {
            BioOutputCard(
                title = "mRNA Transcript (T -> U)",
                content = analysis.transcribedRna,
                onCopy = {
                    clipboard.setText(AnnotatedString(analysis.transcribedRna))
                    Toast.makeText(context, "Copied mRNA transcript", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Protein Translation Frame +1
        item {
            BioOutputCard(
                title = "Translated Polypeptide (Reading Frame +1)",
                content = analysis.translatedProteinFrame1,
                onCopy = {
                    clipboard.setText(AnnotatedString(analysis.translatedProteinFrame1))
                    Toast.makeText(context, "Copied Protein Frame +1", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Ask AI Button
        item {
            CyberButton(
                text = "💬 Consult WESS PLUS AI on this Sequence",
                onClick = {
                    onSendQueryToChat("Analyze this DNA sequence and explain its translation and biological characteristics: ${sequenceInput.take(200)}")
                },
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// 2. Protein Inspector & Hydropathy Section
// -------------------------------------------------------------
@Composable
private fun ProteinInspectorSection(onSendQueryToChat: (String) -> Unit) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var proteinInput by remember { mutableStateOf(BioScienceEngine.SampleProteinInsulinB) }
    val result = remember(proteinInput) { BioScienceEngine.analyzeProtein(proteinInput) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🔬 PROTEIN & PEPTIDE BIOCHEMISTRY INSPECTOR",
                color = colors.cyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Calculates molecular weight, theoretical Isoelectric Point (pI), net charge at pH 7.4, and plots Kyte-Doolittle hydropathy profile.",
                color = colors.muted,
                fontSize = 12.sp
            )
        }

        item {
            OutlinedTextField(
                value = proteinInput,
                onValueChange = { proteinInput = it },
                label = { Text("Amino Acid Sequence (1-letter code)", color = colors.cyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.cyan,
                    unfocusedBorderColor = colors.line,
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink,
                    cursorColor = colors.cyan
                ),
                shape = CyberCutShape,
                modifier = Modifier.fillMaxWidth().testTag("protein_sequence_input"),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Residues", "${result.length} aa", colors.cyan, Modifier.weight(1f))
                StatCard("Mol. Wt.", "${result.molecularWeightDa} Da", colors.ink, Modifier.weight(1f))
                StatCard("Isoelectric (pI)", "${result.isoelectricPoint}", colors.magenta, Modifier.weight(1f))
                StatCard("Net Charge", "${result.netChargePh74} (pH 7.4)", if (result.netChargePh74 >= 0) colors.cyan else colors.magenta, Modifier.weight(1f))
            }
        }

        // Hydropathy Chart Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberCutShape)
                    .background(colors.panel)
                    .border(1.dp, colors.line, CyberCutShape)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Kyte-Doolittle Hydropathy Profile",
                            color = colors.cyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "+ Hydrophobic / - Hydrophilic",
                            color = colors.muted,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (result.hydropathyScores.isNotEmpty()) {
                        HydropathyPlot(scores = result.hydropathyScores, primaryColor = colors.cyan, secondaryColor = colors.magenta)
                    } else {
                        Text("Enter a valid sequence to plot hydropathy.", color = colors.muted, fontSize = 12.sp)
                    }
                }
            }
        }

        // Amino acid composition list
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberCutShape)
                    .background(colors.panel)
                    .border(1.dp, colors.line, CyberCutShape)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Amino Acid Residue Breakdown:",
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.composition.entries.joinToString("  ") { "${it.key}: ${it.value}" },
                        color = colors.cyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            CyberButton(
                text = "Ask AI: Explain Secondary Structure & Binding",
                onClick = {
                    onSendQueryToChat("Explain the structural characteristics and possible binding properties of this peptide sequence: $proteinInput (MW: ${result.molecularWeightDa} Da, pI: ${result.isoelectricPoint})")
                },
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// 3. CRISPR-Cas9 Target Finder Section
// -------------------------------------------------------------
@Composable
private fun CrisprFinderSection(onSendQueryToChat: (String) -> Unit) {
    val colors = LocalCyberColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var dnaTargetInput by remember { mutableStateOf(BioScienceEngine.SampleDnaGfp.take(120)) }
    val guides = remember(dnaTargetInput) { BioScienceEngine.findCrisprGuides(dnaTargetInput) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "✂️ CRISPR-CAS9 SGRNA TARGET FINDER",
                color = colors.cyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scans sequence for SpCas9 PAM sites (5'-NGG-3' on sense or 5'-CCN-3' on antisense). Evaluates on-target GC content and poly-T flags.",
                color = colors.muted,
                fontSize = 12.sp
            )
        }

        item {
            OutlinedTextField(
                value = dnaTargetInput,
                onValueChange = { dnaTargetInput = it },
                label = { Text("Gene / Target Sequence (min 23 nt)", color = colors.cyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.cyan,
                    unfocusedBorderColor = colors.line,
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink,
                    cursorColor = colors.cyan
                ),
                shape = CyberCutShape,
                modifier = Modifier.fillMaxWidth().testTag("crispr_input"),
                minLines = 2,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
        }

        item {
            Text(
                text = "Found ${guides.size} candidate sgRNA guides:",
                color = colors.ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (guides.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberCutShape)
                        .background(colors.panel)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No NGG PAM sites detected. Ensure sequence is at least 23 bp.",
                        color = colors.muted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(guides.take(10)) { guide ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberSmallCutShape)
                        .background(colors.panel)
                        .border(1.dp, if (guide.qualityScore >= 80) colors.cyan else colors.line, CyberSmallCutShape)
                        .clickable {
                            clipboard.setText(AnnotatedString(guide.protospacer))
                            Toast.makeText(context, "Copied guide: ${guide.protospacer}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Strand ${guide.strand} | Pos: ${guide.startPosition}",
                                color = colors.muted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Score: ${guide.qualityScore}/100",
                                color = if (guide.qualityScore >= 80) colors.cyan else colors.magenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "5'-${guide.protospacer}-3'",
                            color = colors.ink,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAM: ${guide.pam}  |  GC%: ${guide.gcPercent}%",
                                color = colors.cyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(guide.protospacer))
                                    Toast.makeText(context, "Copied guide sequence", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy guide",
                                    tint = colors.cyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            CyberButton(
                text = "Ask AI: Review CRISPR Strategy & Off-Targets",
                onClick = {
                    val top = guides.firstOrNull()?.protospacer ?: "guide"
                    onSendQueryToChat("Evaluate this candidate CRISPR sgRNA guide for gene knockout efficiency: $top (Target: ${dnaTargetInput.take(100)})")
                },
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// 4. Lab Dilution & PCR Calculator Section
// -------------------------------------------------------------
@Composable
private fun LabCalculatorSection() {
    val colors = LocalCyberColors.current
    var c1Text by remember { mutableStateOf("100") }
    var c2Text by remember { mutableStateOf("10") }
    var v2Text by remember { mutableStateOf("500") }

    val c1 = c1Text.toFloatOrNull() ?: 0f
    val c2 = c2Text.toFloatOrNull() ?: 0f
    val v2 = v2Text.toFloatOrNull() ?: 0f
    val dilution = remember(c1, c2, v2) { BioScienceEngine.calculateDilution(c1, v2, c2) }

    var pcrRxnCountText by remember { mutableStateOf("10") }
    val pcrRxnCount = (pcrRxnCountText.toIntOrNull() ?: 1).coerceAtLeast(1)
    var pcrVolText by remember { mutableStateOf("50") }
    val pcrVol = (pcrVolText.toFloatOrNull() ?: 50f).coerceAtLeast(1f)
    val pcrMix = remember(pcrRxnCount, pcrVol) { BioScienceEngine.generatePcrMasterMix(pcrRxnCount, pcrVol) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "🧪 LABORATORY DILUTION & REACTION MASTER MIX",
                color = colors.cyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // C1 V1 = C2 V2 Calculator Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberCutShape)
                    .background(colors.panel)
                    .border(1.dp, colors.cyan, CyberCutShape)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Stock Solution Dilution (C₁V₁ = C₂V₂)",
                        color = colors.cyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = c1Text,
                            onValueChange = { c1Text = it },
                            label = { Text("Stock C1", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = CyberSmallCutShape,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = c2Text,
                            onValueChange = { c2Text = it },
                            label = { Text("Target C2", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = CyberSmallCutShape,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = v2Text,
                            onValueChange = { v2Text = it },
                            label = { Text("Target V2", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = CyberSmallCutShape,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CyberSmallCutShape)
                            .background(colors.void.copy(alpha = 0.6f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Formula: ${dilution.formula}",
                                color = colors.muted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pipette: ${dilution.stockVolumeNeeded} units Stock (C1)",
                                color = colors.magenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Add: ${dilution.diluentVolumeNeeded} units Diluent/Water",
                                color = colors.cyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // PCR Master Mix Recipe Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberCutShape)
                    .background(colors.panel)
                    .border(1.dp, colors.magenta, CyberCutShape)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. PCR Master Mix Generator",
                            color = colors.magenta,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "+10% pipetting excess",
                            color = colors.muted,
                            fontSize = 10.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pcrRxnCountText,
                            onValueChange = { pcrRxnCountText = it },
                            label = { Text("Reactions (N)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = CyberSmallCutShape,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pcrVolText,
                            onValueChange = { pcrVolText = it },
                            label = { Text("Vol/rxn (µL)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = CyberSmallCutShape,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Recipe table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CyberSmallCutShape)
                            .background(colors.void.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Component", color = colors.muted, fontSize = 11.sp, modifier = Modifier.weight(2f))
                            Text("1x (µL)", color = colors.muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("Total (µL)", color = colors.cyan, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        }

                        pcrMix.forEach { comp ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(comp.name, color = colors.ink, fontSize = 11.sp, modifier = Modifier.weight(2f))
                                Text("${comp.volumePerReactionUl}", color = colors.ink, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${comp.totalVolumeUl}", color = colors.cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// -------------------------------------------------------------
// 5. Bio Protocols & Reference Section
// -------------------------------------------------------------
@Composable
private fun BioProtocolsSection(onSendQueryToChat: (String) -> Unit) {
    val colors = LocalCyberColors.current
    val guides = remember { BioScienceEngine.BioReferenceGuides }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "📚 BIOSCIENCE PROTOCOLS & BENCH GUIDES",
                color = colors.cyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quick reference protocols verified against standard molecular biology procedures.",
                color = colors.muted,
                fontSize = 12.sp
            )
        }

        items(guides) { guide ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberCutShape)
                    .background(colors.panel)
                    .border(1.dp, colors.line, CyberCutShape)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = guide.title,
                            color = colors.cyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(CyberSmallCutShape)
                                .background(colors.magenta.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = guide.category,
                                color = colors.magenta,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = guide.summary,
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = guide.details,
                        color = colors.muted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    CyberButton(
                        text = "Troubleshoot / Ask AI about ${guide.title}",
                        onClick = {
                            onSendQueryToChat("Explain the protocol, key troubleshooting steps, and optimization parameters for ${guide.title} in molecular biology.")
                        },
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// -------------------------------------------------------------
// Helper UI Components
// -------------------------------------------------------------
@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    val colors = LocalCyberColors.current
    Box(
        modifier = modifier
            .clip(CyberSmallCutShape)
            .background(colors.panel)
            .border(1.dp, colors.line, CyberSmallCutShape)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = colors.muted, fontSize = 9.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                color = valueColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BioOutputCard(
    title: String,
    content: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCyberColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CyberSmallCutShape)
            .background(colors.panel)
            .border(1.dp, colors.line, CyberSmallCutShape)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.cyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.cyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = if (content.isEmpty()) "(none)" else content,
                color = colors.ink,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun HydropathyPlot(scores: List<Float>, primaryColor: Color, secondaryColor: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw baseline (0 hydropathy threshold)
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1.dp.toPx()
        )

        if (scores.isEmpty()) return@Canvas
        if (scores.size == 1) {
            val score = scores[0]
            val y = midY - (score / 4.5f) * (height / 2f)
            val pointColor = if (score >= 0) primaryColor else secondaryColor
            drawCircle(color = pointColor, radius = 4.dp.toPx(), center = Offset(width / 2f, y))
            return@Canvas
        }

        val maxVal = 4.5f
        val stepX = width / (scores.size - 1).toFloat()

        val path = Path()
        scores.forEachIndexed { i, score ->
            val x = i * stepX
            // Map score [-4.5, +4.5] to [height, 0]
            val y = midY - (score / maxVal) * (height / 2f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw individual point peaks
        scores.forEachIndexed { i, score ->
            val x = i * stepX
            val y = midY - (score / maxVal) * (height / 2f)
            val pointColor = if (score >= 0) primaryColor else secondaryColor
            drawCircle(
                color = pointColor,
                radius = 2.5.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
