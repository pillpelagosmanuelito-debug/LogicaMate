package com.educalab.logicamate.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PieceSpecTest {

    @Test
    fun `encode then decode round-trips a full piece`() {
        val piece = PieceSpec(shape = Shape.HEXAGON, color = PieceColor.EMBER, size = PieceSize.LARGE, count = 3, value = null)
        assertEquals(piece, PieceSpec.decode(piece.encode()))
    }

    @Test
    fun `encode then decode round-trips a piece with a numeric value`() {
        val piece = PieceSpec(shape = Shape.NONE, value = 42)
        val decoded = PieceSpec.decode(piece.encode())
        assertEquals(42, decoded.value)
    }

    @Test
    fun `blank piece encodes and decodes to the same singleton-equivalent value`() {
        val blank = PieceSpec.BLANK
        assertEquals(blank, PieceSpec.decode(blank.encode()))
        assertTrue(PieceSpec.decode("BLANK").isBlank)
    }

    @Test
    fun `encode is stable for equal pieces`() {
        val a = PieceSpec(shape = Shape.STAR, color = PieceColor.GOLD, count = 2)
        val b = PieceSpec(shape = Shape.STAR, color = PieceColor.GOLD, count = 2)
        assertEquals(a.encode(), b.encode())
    }

    @Test
    fun `different counts produce different encodings`() {
        val a = PieceSpec(count = 1)
        val b = PieceSpec(count = 2)
        assertTrue(a.encode() != b.encode())
    }
}
