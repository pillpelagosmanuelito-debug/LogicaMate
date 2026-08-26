package com.educalab.logicamate.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SequenceEngineTest {

    @Test
    fun `arithmetic sequence detects constant step`() {
        val rule = SequenceEngine.detectRule(listOf(2, 4, 6, 8))
        assertTrue(rule is SequenceEngine.SequenceRule.Arithmetic)
        assertEquals(2, (rule as SequenceEngine.SequenceRule.Arithmetic).step)
    }

    @Test
    fun `arithmetic sequence predicts next value`() {
        assertEquals(10, SequenceEngine.nextValue(listOf(2, 4, 6, 8)))
    }

    @Test
    fun `geometric sequence detects constant ratio`() {
        val rule = SequenceEngine.detectRule(listOf(3, 6, 12, 24))
        assertTrue(rule is SequenceEngine.SequenceRule.Geometric)
        assertEquals(24 * 2, SequenceEngine.nextValue(listOf(3, 6, 12, 24)))
    }

    @Test
    fun `alternating step sequence detects two interleaved steps`() {
        // 1, 3, 8, 10, 15 -> +2, +5, +2, +5 -> next +2 = 17
        val values = listOf(1, 3, 8, 10, 15)
        assertEquals(17, SequenceEngine.nextValue(values))
    }

    @Test
    fun `ambiguous sequence has no unique solution`() {
        // Ni aritmética, ni geométrica, ni alternante de 2 pasos.
        val values = listOf(1, 2, 4, 7, 12)
        assertFalse(SequenceEngine.hasUniqueSolution(values))
        assertNull(SequenceEngine.nextValue(values))
    }

    @Test
    fun `validateAnswer accepts correct next value`() {
        assertTrue(SequenceEngine.validateAnswer(listOf(5, 10, 15, 20), 25))
    }

    @Test
    fun `validateAnswer rejects incorrect next value`() {
        assertFalse(SequenceEngine.validateAnswer(listOf(5, 10, 15, 20), 30))
    }

    @Test
    fun `constant sequence is treated as arithmetic with step zero`() {
        assertEquals(7, SequenceEngine.nextValue(listOf(7, 7, 7, 7)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `detectRule requires at least three terms`() {
        SequenceEngine.detectRule(listOf(1, 2))
    }

    @Test
    fun `geometric detection does not misfire on arithmetic data`() {
        val rule = SequenceEngine.detectRule(listOf(2, 4, 6, 8))
        assertFalse(rule is SequenceEngine.SequenceRule.Geometric)
    }
}
