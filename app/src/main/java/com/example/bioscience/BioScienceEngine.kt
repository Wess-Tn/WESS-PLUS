package com.example.bioscience

import com.example.model.BioReferenceGuide
import com.example.model.CrisprGuideResult
import com.example.model.DnaAnalysisResult
import com.example.model.LabDilutionResult
import com.example.model.PcrMixComponent
import com.example.model.ProteinAnalysisResult
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

object BioScienceEngine {

    // Standard Genetic Code codon table
    private val CodonTable: Map<String, Char> = mapOf(
        "TTT" to 'F', "TTC" to 'F', "TTA" to 'L', "TTG" to 'L',
        "CTT" to 'L', "CTC" to 'L', "CTA" to 'L', "CTG" to 'L',
        "ATT" to 'I', "ATC" to 'I', "ATA" to 'I', "ATG" to 'M', // Start codon
        "GTT" to 'V', "GTC" to 'V', "GTA" to 'V', "GTG" to 'V',
        "TCT" to 'S', "TCC" to 'S', "TCA" to 'S', "TCG" to 'S',
        "CCT" to 'P', "CCC" to 'P', "CCA" to 'P', "CCG" to 'P',
        "ACT" to 'T', "ACC" to 'T', "ACA" to 'T', "ACG" to 'T',
        "GCT" to 'A', "GCC" to 'A', "GCA" to 'A', "GCG" to 'A',
        "TAT" to 'Y', "TAC" to 'Y', "TAA" to '*', "TAG" to '*', // Stop codons
        "CAT" to 'H', "CAC" to 'H', "CAA" to 'Q', "CAG" to 'Q',
        "AAT" to 'N', "AAC" to 'N', "AAA" to 'K', "AAG" to 'K',
        "GAT" to 'D', "GAC" to 'D', "GAA" to 'E', "GAG" to 'E',
        "TGT" to 'C', "TGC" to 'C', "TGA" to '*', "TGG" to 'W', // Stop TGA
        "CGT" to 'R', "CGC" to 'R', "CGA" to 'R', "CGG" to 'R',
        "AGT" to 'S', "AGC" to 'S', "AGA" to 'R', "AGG" to 'R',
        "GGT" to 'G', "GGC" to 'G', "GGA" to 'G', "GGG" to 'G'
    )

    // Kyte-Doolittle hydropathy scale
    val HydropathyScale = mapOf(
        'I' to 4.5f, 'V' to 4.2f, 'L' to 3.8f, 'F' to 2.8f, 'C' to 2.5f,
        'M' to 1.9f, 'A' to 1.8f, 'G' to -0.4f, 'T' to -0.7f, 'S' to -0.8f,
        'W' to -0.9f, 'Y' to -1.3f, 'P' to -1.6f, 'H' to -3.2f, 'E' to -3.5f,
        'Q' to -3.5f, 'D' to -3.5f, 'N' to -3.5f, 'K' to -3.9f, 'R' to -4.5f
    )

    // Molecular weights of individual amino acid residues (minus H2O)
    private val AminoAcidMw = mapOf(
        'A' to 71.08f, 'R' to 156.2f, 'N' to 114.1f, 'D' to 115.1f, 'C' to 103.1f,
        'E' to 129.1f, 'Q' to 128.1f, 'G' to 57.05f, 'H' to 137.1f, 'I' to 113.2f,
        'L' to 113.2f, 'K' to 128.2f, 'M' to 131.2f, 'F' to 147.2f, 'P' to 97.12f,
        'S' to 87.08f, 'T' to 101.1f, 'W' to 186.2f, 'Y' to 163.2f, 'V' to 99.13f
    )

    // pKa values for Isoelectric point calculation
    private const val PKA_N_TERM = 9.69f
    private const val PKA_C_TERM = 2.34f
    private val PkaSideChains = mapOf(
        'C' to 8.33f, 'D' to 3.86f, 'E' to 4.25f, 'H' to 6.00f,
        'K' to 10.53f, 'R' to 12.48f, 'Y' to 10.07f
    )

    fun cleanDna(raw: String): String {
        return raw.uppercase().filter { it in "ATCGN" }
    }

    fun cleanProtein(raw: String): String {
        return raw.uppercase().filter { it in "ACDEFGHIKLMNPQRSTVWY" }
    }

    fun reverseComplement(dna: String): String {
        val complement = StringBuilder(dna.length)
        for (char in dna.reversed()) {
            val comp = when (char) {
                'A' -> 'T'
                'T' -> 'A'
                'C' -> 'G'
                'G' -> 'C'
                else -> 'N'
            }
            complement.append(comp)
        }
        return complement.toString()
    }

    fun transcribeToRna(dna: String): String {
        return dna.replace('T', 'U')
    }

    fun translateFrame(dna: String, offset: Int): String {
        val sb = StringBuilder()
        var i = offset
        while (i + 2 < dna.length) {
            val codon = dna.substring(i, i + 3)
            val aa = CodonTable[codon] ?: 'X'
            sb.append(aa)
            i += 3
        }
        return sb.toString()
    }

    fun analyzeDna(rawInput: String): DnaAnalysisResult {
        val dna = cleanDna(rawInput)
        val len = dna.length
        if (len == 0) {
            return DnaAnalysisResult(0, 0, 0f, 0f, 0f, 0f, "", "", "", "", "")
        }

        val gCount = dna.count { it == 'G' }
        val cCount = dna.count { it == 'C' }
        val aCount = dna.count { it == 'A' }
        val tCount = dna.count { it == 'T' }
        val gcCount = gCount + cCount
        val gcPercent = (gcCount.toFloat() / len) * 100f
        val atPercent = ((aCount + tCount).toFloat() / len) * 100f

        // Melting Temperature Tm
        val tm = if (len < 14) {
            (2 * (aCount + tCount) + 4 * gcCount).toFloat()
        } else {
            64.9f + 41.0f * (gcCount - 16.4f) / len
        }

        // Molecular Weight (approx 650 Da per dsDNA bp or ~330 Da per ssDNA nt)
        val mwKDa = (len * 330f) / 1000f

        val revComp = reverseComplement(dna)
        val rna = transcribeToRna(dna)
        val frame1 = translateFrame(dna, 0)
        val frame2 = translateFrame(dna, 1)
        val frame3 = translateFrame(dna, 2)

        return DnaAnalysisResult(
            length = len,
            gcCount = gcCount,
            gcPercent = (gcPercent * 10).roundToInt() / 10f,
            atPercent = (atPercent * 10).roundToInt() / 10f,
            molecularWeightKDa = (mwKDa * 100).roundToInt() / 100f,
            meltingTempTm = (tm * 10).roundToInt() / 10f,
            reverseComplement = revComp,
            transcribedRna = rna,
            translatedProteinFrame1 = frame1,
            translatedProteinFrame2 = frame2,
            translatedProteinFrame3 = frame3
        )
    }

    fun analyzeProtein(rawInput: String): ProteinAnalysisResult {
        val protein = cleanProtein(rawInput)
        val len = protein.length
        if (len == 0) {
            return ProteinAnalysisResult(0, 0f, 7.0f, 0f, emptyMap(), emptyList())
        }

        val comp = mutableMapOf<Char, Int>()
        var totalMw = 18.02f // terminal H2O
        protein.forEach { aa ->
            comp[aa] = (comp[aa] ?: 0) + 1
            totalMw += AminoAcidMw[aa] ?: 110f
        }

        // Hydropathy score sliding window (window size 5)
        val window = 5
        val hydropathyScores = mutableListOf<Float>()
        for (i in 0 until (len - window + 1)) {
            val sub = protein.substring(i, i + window)
            val avg = sub.map { HydropathyScale[it] ?: 0f }.average().toFloat()
            hydropathyScores.add(avg)
        }
        if (hydropathyScores.isEmpty()) {
            hydropathyScores.addAll(protein.map { HydropathyScale[it] ?: 0f })
        }

        // Net Charge at pH 7.4
        val chargeAt74 = calculateNetCharge(protein, 7.4f)

        // Calculate Isoelectric Point (pI) using bisection
        val pI = calculateIsoelectricPoint(protein)

        return ProteinAnalysisResult(
            length = len,
            molecularWeightDa = (totalMw * 10).roundToInt() / 10f,
            isoelectricPoint = (pI * 100).roundToInt() / 100f,
            netChargePh74 = (chargeAt74 * 100).roundToInt() / 100f,
            composition = comp,
            hydropathyScores = hydropathyScores
        )
    }

    private fun calculateNetCharge(protein: String, pH: Float): Float {
        // N-term positive charge
        var charge = 1.0f / (1.0f + 10.0f.pow(pH - PKA_N_TERM))
        // C-term negative charge
        charge -= 1.0f / (1.0f + 10.0f.pow(PKA_C_TERM - pH))

        protein.forEach { aa ->
            when (aa) {
                'K' -> charge += 1.0f / (1.0f + 10.0f.pow(pH - PkaSideChains.getValue('K')))
                'R' -> charge += 1.0f / (1.0f + 10.0f.pow(pH - PkaSideChains.getValue('R')))
                'H' -> charge += 1.0f / (1.0f + 10.0f.pow(pH - PkaSideChains.getValue('H')))
                'D' -> charge -= 1.0f / (1.0f + 10.0f.pow(PkaSideChains.getValue('D') - pH))
                'E' -> charge -= 1.0f / (1.0f + 10.0f.pow(PkaSideChains.getValue('E') - pH))
                'C' -> charge -= 1.0f / (1.0f + 10.0f.pow(PkaSideChains.getValue('C') - pH))
                'Y' -> charge -= 1.0f / (1.0f + 10.0f.pow(PkaSideChains.getValue('Y') - pH))
            }
        }
        return charge
    }

    private fun calculateIsoelectricPoint(protein: String): Float {
        var minPh = 0.0f
        var maxPh = 14.0f
        var pI = 7.0f

        for (step in 0..25) {
            pI = (minPh + maxPh) / 2.0f
            val charge = calculateNetCharge(protein, pI)
            if (abs(charge) < 0.001f) break
            if (charge > 0) {
                minPh = pI
            } else {
                maxPh = pI
            }
        }
        return pI
    }

    // CRISPR Guide RNA target finder (finds NGG PAMs and extracts 20nt protospacer)
    fun findCrisprGuides(rawDna: String): List<CrisprGuideResult> {
        val dna = cleanDna(rawDna)
        val guides = mutableListOf<CrisprGuideResult>()

        // Sense strand: look for 20nt + [A/C/G/T]GG
        for (i in 20 until (dna.length - 2)) {
            if (dna[i + 1] == 'G' && dna[i + 2] == 'G') {
                val protospacer = dna.substring(i - 20, i)
                val pam = dna.substring(i, i + 3)
                val gc = protospacer.count { it == 'G' || it == 'C' } * 5f
                val qualityScore = calculateCrisprScore(gc, protospacer)
                guides.add(
                    CrisprGuideResult(
                        protospacer = protospacer,
                        pam = pam,
                        strand = "+",
                        startPosition = i - 20 + 1,
                        gcPercent = gc,
                        qualityScore = qualityScore
                    )
                )
            }
        }

        // Antisense strand: look for CC[A/C/G/T] + 20nt (which corresponds to NGG on reverse strand)
        for (i in 0 until (dna.length - 22)) {
            if (dna[i] == 'C' && dna[i + 1] == 'C') {
                val revPamTarget = dna.substring(i + 3, i + 23)
                val protospacer = reverseComplement(revPamTarget)
                val pam = reverseComplement(dna.substring(i, i + 3))
                val gc = protospacer.count { it == 'G' || it == 'C' } * 5f
                val qualityScore = calculateCrisprScore(gc, protospacer)
                guides.add(
                    CrisprGuideResult(
                        protospacer = protospacer,
                        pam = pam,
                        strand = "-",
                        startPosition = i + 1,
                        gcPercent = gc,
                        qualityScore = qualityScore
                    )
                )
            }
        }

        return guides.sortedByDescending { it.qualityScore }
    }

    private fun calculateCrisprScore(gc: Float, protospacer: String): Int {
        var score = 85
        // Optimal GC is between 40% and 60%
        if (gc in 40f..60f) {
            score += 10
        } else if (gc < 30f || gc > 75f) {
            score -= 25
        }

        // Avoid poly-T tract (causes premature pol III transcription termination)
        if (protospacer.contains("TTTT")) {
            score -= 35
        }
        // Guanine preferred at pos 20 (adjacent to PAM)
        if (protospacer.last() == 'G') {
            score += 5
        }
        return score.coerceIn(10, 99)
    }

    // C1 V1 = C2 V2 lab dilution calculator
    fun calculateDilution(c1: Float, v2: Float, c2: Float): LabDilutionResult {
        if (c1 <= 0f || v2 <= 0f || c2 <= 0f) {
            return LabDilutionResult("C1 × V1 = C2 × V2", 0f, 0f, "Enter positive values.")
        }
        if (c2 > c1) {
            return LabDilutionResult(
                "C1 × V1 = C2 × V2",
                0f,
                0f,
                "Target concentration C2 cannot be higher than stock C1."
            )
        }

        val v1 = (c2 * v2) / c1
        val vDiluent = v2 - v1
        val explanation = "Pipette ${(v1 * 100).roundToInt() / 100f} of stock (C1) and add ${(vDiluent * 100).roundToInt() / 100f} of buffer/water to reach ${(v2 * 100).roundToInt() / 100f} total volume."

        return LabDilutionResult(
            formula = "V1 = (C2 × V2) / C1 = ($c2 × $v2) / $c1",
            stockVolumeNeeded = (v1 * 100).roundToInt() / 100f,
            diluentVolumeNeeded = (vDiluent * 100).roundToInt() / 100f,
            explanation = explanation
        )
    }

    // PCR Master Mix Recipe Generator
    fun generatePcrMasterMix(reactionsCount: Int, reactionVolumeUl: Float): List<PcrMixComponent> {
        val totalRxns = (reactionsCount * 1.1f) // 10% pipetting excess
        val scale = reactionVolumeUl / 50f // standard 50 uL recipe ratio

        return listOf(
            PcrMixComponent(
                name = "Nuclease-Free H2O",
                stockConcentration = "Sterile",
                finalConcentration = "-",
                volumePerReactionUl = (33.5f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (33.5f * scale * totalRxns * 10).roundToInt() / 10f
            ),
            PcrMixComponent(
                name = "10X PCR Reaction Buffer",
                stockConcentration = "10X",
                finalConcentration = "1X",
                volumePerReactionUl = (5.0f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (5.0f * scale * totalRxns * 10).roundToInt() / 10f
            ),
            PcrMixComponent(
                name = "10 mM dNTP Mix",
                stockConcentration = "10 mM each",
                finalConcentration = "0.2 mM",
                volumePerReactionUl = (1.0f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (1.0f * scale * totalRxns * 10).roundToInt() / 10f
            ),
            PcrMixComponent(
                name = "Forward Primer",
                stockConcentration = "10 µM",
                finalConcentration = "0.2 µM",
                volumePerReactionUl = (1.0f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (1.0f * scale * totalRxns * 10).roundToInt() / 10f
            ),
            PcrMixComponent(
                name = "Reverse Primer",
                stockConcentration = "10 µM",
                finalConcentration = "0.2 µM",
                volumePerReactionUl = (1.0f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (1.0f * scale * totalRxns * 10).roundToInt() / 10f
            ),
            PcrMixComponent(
                name = "DNA Template",
                stockConcentration = "~10-50 ng/µL",
                finalConcentration = "Variable",
                volumePerReactionUl = (2.0f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (2.0f * scale * totalRxns * 10).roundToInt() / 10f
            ),
            PcrMixComponent(
                name = "Taq / Phusion Polymerase",
                stockConcentration = "5 U/µL",
                finalConcentration = "1.25 U/rxn",
                volumePerReactionUl = (0.5f * scale * 10).roundToInt() / 10f,
                totalVolumeUl = (0.5f * scale * totalRxns * 10).roundToInt() / 10f
            )
        )
    }

    // Sample Presets for one-tap analysis
    val SampleDnaGfp = "ATGGTGAGCAAGGGCGAGGAGCTGTTCACCGGGGTGGTGCCCATCCTGGTCGAGCTGGACGGCGACGTAAACGGCCACAAGTTCAGCGTGTCCGGCGAGGGCGAGGGCGATGCCACCTACGGCAAGCTGACCCTGAAGTTCATCTGCACCACCGGCAAGCTGCCCGTGCCCTGGCCCACCCTCGTGACCACCCTGACCTACGGCGTGCAGTGCTTCAGCCGCTACCCCGACCACATGAAGCAGCACGACTTCTTCAAG"
    val SampleDnaInsulin = "ATGGCCCTGTGGATGCGCCTCCTGCCCCTGCTGGCGCTGCTGGCCCTCTGGGGACCTGACCCAGCCGCAGCCTTTGTGAACCAACACCTGTGCGGCTCACACCTGGTGGAAGCTCTCTACCTAGTGTGCGGGGAACGAGGCTTCTTCTACACACCCAAGACCCGCCGGGAGGCAGAGGACCTGCAGG"
    val SampleProteinInsulinB = "FVNQHLCGSHLVEALYLVCGERGFFYTPKT"

    val BioReferenceGuides = listOf(
        BioReferenceGuide(
            title = "CRISPR-Cas9 Gene Editing",
            category = "Genomics",
            summary = "Targeted double-strand breaks guided by 20nt protospacer adjacent to NGG PAM motif.",
            details = "Cas9 from S. pyogenes requires a 20-nucleotide guide sequence matching the target DNA, followed by an SpCas9 PAM (5'-NGG-3'). Cleavage occurs 3-4 base pairs upstream of the PAM. Repairs proceed via Non-Homologous End Joining (NHEJ) introducing indels, or Homology-Directed Repair (HDR) with a donor template for precise insertion."
        ),
        BioReferenceGuide(
            title = "PCR Cycling Protocol",
            category = "Molecular Biology",
            summary = "Standard 3-step thermal cycling protocol: Denaturation, Annealing, and Extension.",
            details = "1. Initial Denaturation: 95°C for 2-5 min.\n2. 25-35 Cycles:\n   • Denaturation: 95°C for 30s\n   • Annealing: 5°C below primer Tm (typically 52-60°C) for 30s\n   • Extension: 72°C (1 min per kb for Taq, 15-30s per kb for high-fidelity enzymes)\n3. Final Extension: 72°C for 5-10 min.\n4. Hold: 4°C."
        ),
        BioReferenceGuide(
            title = "Protein Isoelectric Point (pI)",
            category = "Biochemistry",
            summary = "The pH at which a protein carries zero net electrical charge.",
            details = "At pH < pI, the protein is positively charged and binds cation-exchange matrices. At pH > pI, the protein carries net negative charge and binds anion exchangers. Near the pI, solubility is minimized and precipitation or aggregation can occur."
        ),
        BioReferenceGuide(
            title = "Michaelis-Menten Kinetics",
            category = "Enzymology",
            summary = "Mathematical model describing the rate of enzymatic reactions.",
            details = "V = (Vmax × [S]) / (Km + [S]).\n• Km (Michaelis constant) is substrate concentration at half-maximal velocity (Vmax/2); lower Km indicates higher substrate affinity.\n• kcat = Vmax / [E]total (turnover number).\n• Catalytic efficiency is measured by kcat / Km."
        ),
        BioReferenceGuide(
            title = "Central Dogma & Genetic Code",
            category = "Genetics",
            summary = "DNA transcription to mRNA and translation to polypeptides with 64 triplet codons.",
            details = "Standard start codon: ATG (Methionine). Stop codons: TAA (Ochre), TAG (Amber), TGA (Opal). Degeneracy: 61 sense codons encode 20 amino acids with wobble base pairing at the 3rd codon position."
        )
    )
}
