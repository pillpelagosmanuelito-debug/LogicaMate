package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternEngineTest {

    private fun colorPiece(c: PieceColor) = PieceSpec(shape = Shape.CIRCLE, color = c)

    @Test
    fun `detects period-2 cycle`() {
        val items = listOf(
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL),
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL),
        )
        assertEquals(2, PatternEngine.detectCycleLength(items))
    }

    @Test
    fun `detects period-3 cycle`() {
        val items = listOf(
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL), colorPiece(PieceColor.CORAL),
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL), colorPiece(PieceColor.CORAL),
        )
        assertEquals(3, PatternEngine.detectCycleLength(items))
    }

    @Test
    fun `nextInCycle returns the piece that continues the pattern`() {
        val items = listOf(
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CORAL),
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CORAL),
        )
        assertEquals(colorPiece(PieceColor.GOLD), PatternEngine.nextInCycle(items))
    }

    @Test
    fun `single repetition is not enough to declare a cycle`() {
        val items = listOf(colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL))
        assertNull(PatternEngine.detectCycleLength(items))
    }

    @Test
    fun `non-cyclic sequence returns null`() {
        val items = listOf(
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL),
            colorPiece(PieceColor.CORAL), colorPiece(PieceColor.STONE),
        )
        assertNull(PatternEngine.detectCycleLength(items))
    }

    @Test
    fun `validateAnswer accepts the correct continuation`() {
        val items = listOf(
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL),
            colorPiece(PieceColor.GOLD), colorPiece(PieceColor.CRYSTAL),
        )
        assertTrue(PatternEngine.validateAnswer(items, colorPiece(PieceColor.GOLD)))
    }

    @Test
    fun `multi-property pattern combines independent shape and color cycles`() {
        val shapeCycle = listOf(Shape.TRIANGLE, Shape.CIRCLE, Shape.SQUARE)
        val colorCycle = listOf(PieceColor.GOLD, PieceColor.CRYSTAL)
        val items = (0 until 6).map {
            PieceSpec(shape = shapeCycle[it % 3], color = colorCycle[it % 2], size = PieceSize.MEDIUM)
        }
        val next = PatternEngine.nextMultiProperty(items)
        assertEquals(PieceSpec(shape = Shape.TRIANGLE, color = PieceColor.GOLD, size = PieceSize.MEDIUM), next)
    }

    @Test
    fun `multi-property pattern returns null when one stream has no cycle`() {
        val items = listOf(
            PieceSpec(shape = Shape.TRIANGLE, color = PieceColor.GOLD),
            PieceSpec(shape = Shape.CIRCLE, color = PieceColor.CRYSTAL),
            PieceSpec(shape = Shape.SQUARE, color = PieceColor.CORAL),
            PieceSpec(shape = Shape.STAR, color = PieceColor.STONE),
        )
        assertNull(PatternEngine.nextMultiProperty(items))
    }
}
