package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalogyEngineTest {

    @Test
    fun `triangle doubles into two triangles, so circle doubles into two circles`() {
        val a = PieceSpec(shape = Shape.TRIANGLE, count = 1)
        val aPrime = PieceSpec(shape = Shape.TRIANGLE, count = 2)
        val b = PieceSpec(shape = Shape.CIRCLE, count = 1)
        val transform = MatrixEngine.CellTransform.MultiplyCount(2)
        assertTrue(AnalogyEngine.validateAnswer(a, aPrime, transform, b, PieceSpec(shape = Shape.CIRCLE, count = 2)))
    }

    @Test
    fun `wrong transform is rejected even if it explains the pair by coincidence`() {
        val a = PieceSpec(shape = Shape.TRIANGLE, count = 2)
        val aPrime = PieceSpec(shape = Shape.TRIANGLE, count = 4)
        val wrongTransform = MatrixEngine.CellTransform.MultiplyCount(3) // 2*3=6 != 4
        assertFalse(AnalogyEngine.transformExplainsPair(a, aPrime, wrongTransform))
    }

    @Test
    fun `color cycle analogy applies the same step to a different base color`() {
        val order = listOf(PieceColor.GOLD, PieceColor.CRYSTAL, PieceColor.CORAL)
        val transform = MatrixEngine.CellTransform.CycleColor(order)
        val a = PieceSpec(color = PieceColor.GOLD)
        val aPrime = PieceSpec(color = PieceColor.CRYSTAL)
        val b = PieceSpec(color = PieceColor.CORAL)
        val expected = PieceSpec(color = PieceColor.GOLD) // CORAL -> wraps to GOLD
        assertTrue(AnalogyEngine.validateAnswer(a, aPrime, transform, b, expected))
    }

    @Test
    fun `incorrect candidate answer is rejected`() {
        val a = PieceSpec(shape = Shape.STAR, count = 1)
        val aPrime = PieceSpec(shape = Shape.STAR, count = 2)
        val b = PieceSpec(shape = Shape.HEXAGON, count = 1)
        val transform = MatrixEngine.CellTransform.MultiplyCount(2)
        assertFalse(AnalogyEngine.validateAnswer(a, aPrime, transform, b, PieceSpec(shape = Shape.HEXAGON, count = 3)))
    }
}
