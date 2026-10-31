package com.example

import com.example.bioscience.BioScienceEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BioScienceEngineTest {

    @Test
    fun testReverseComplement() {
        val dna = "ATGC"
        val revComp = BioScienceEngine.reverseComplement(dna)
        assertEquals("GCAT", revComp)
    }

    @Test
    fun testTranscription() {
        val dna = "ATGCATG"
        val rna = BioScienceEngine.transcribeToRna(dna)
        assertEquals("AUGCAUG", rna)
    }

    @Test
    fun testTranslation() {
        val dna = "ATGGTGAGCAAG"
        val protein = BioScienceEngine.translateFrame(dna, 0)
        // ATG = M, GTG = V, AGC = S, AAG = K
        assertEquals("MVSK", protein)
    }

    @Test
    fun testDnaAnalysis() {
        val dna = "GGCC"
        val result = BioScienceEngine.analyzeDna(dna)
        assertEquals(4, result.length)
        assertEquals(100f, result.gcPercent, 0.1f)
    }

    @Test
    fun testCrisprGuides() {
        // 20 nt protospacer + CGG PAM
        val target = "ATCGATCGATCGATCGATCGCGG"
        val guides = BioScienceEngine.findCrisprGuides(target)
        assertTrue(guides.isNotEmpty())
        assertEquals("ATCGATCGATCGATCGATCG", guides.first().protospacer)
        assertEquals("CGG", guides.first().pam)
    }

    @Test
    fun testDilutionCalculation() {
        // C1 = 100 mM, C2 = 10 mM, V2 = 500 mL -> V1 = (10 * 500) / 100 = 50 mL
        val res = BioScienceEngine.calculateDilution(100f, 500f, 10f)
        assertEquals(50f, res.stockVolumeNeeded, 0.01f)
        assertEquals(450f, res.diluentVolumeNeeded, 0.01f)
    }
}
