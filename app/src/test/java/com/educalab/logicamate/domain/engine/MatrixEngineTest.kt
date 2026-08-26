package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatrixEngineTest {

    @Test
    fun `multiply count transform doubles each row`() {
        val baseRow = listOf(PieceSpec(shape = Shape.TRIANGLE, count = 1), PieceSpec(shape = Shape.CIRCLE, count = 1))
        val matrix = MatrixEngine.buildMatrix(baseRow, MatrixEngine.CellTransform.MultiplyCount(2), rows = 3)
        assertEquals(1, matrix[0][0].count)
        assertEquals(2, matrix[1][0].count)
        assertEquals(4, matrix[2][0].count)
    }

    @Test
    fun `solveCell matches example from the spec (triangle doubling)`() {
        val baseRow = listOf(PieceSpec(shape = Shape.TRIANGLE, count = 1), PieceSpec(shape = Shape.CIRCLE, count = 1))
        val transform = MatrixEngine.CellTransform.MultiplyCount(2)
        val solved = MatrixEngine.solveCell(baseRow, transform, row = 1, col = 0)
        assertEquals(PieceSpec(shape = Shape.TRIANGLE, count = 2), solved)
    }

    @Test
    fun `cycle size transform advances through small medium large`() {
        val piece = PieceSpec(size = PieceSize.SMALL)
        val transform = MatrixEngine.CellTransform.CycleSize()
        assertEquals(PieceSize.MEDIUM, transform.apply(piece).size)
        assertEquals(PieceSize.LARGE, transform.applyTimes(piece, 2).size)
        assertEquals(PieceSize.SMALL, transform.applyTimes(piece, 3).size) // vuelve a empezar
    }

    @Test
    fun `cycle color transform wraps around a custom order`() {
        val order = listOf(PieceColor.GOLD, PieceColor.CRYSTAL, PieceColor.CORAL)
        val transform = MatrixEngine.CellTransform.CycleColor(order)
        val piece = PieceSpec(color = PieceColor.CORAL)
        assertEquals(PieceColor.GOLD, transform.apply(piece).color)
    }

    @Test
    fun `validateAnswer accepts the correct cell and rejects a wrong one`() {
        val baseRow = listOf(PieceSpec(shape = Shape.STAR, count = 1))
        val transform = MatrixEngine.CellTransform.MultiplyCount(2)
        assertTrue(MatrixEngine.validateAnswer(baseRow, transform, row = 2, col = 0, candidate = PieceSpec(shape = Shape.STAR, count = 4)))
        assertFalse(MatrixEngine.validateAnswer(baseRow, transform, row = 2, col = 0, candidate = PieceSpec(shape = Shape.STAR, count = 3)))
    }

    @Test
    fun `cycle shape transform respects declared order`() {
        val order = listOf(Shape.TRIANGLE, Shape.CIRCLE, Shape.SQUARE)
        val transform = MatrixEngine.CellTransform.CycleShape(order)
        assertEquals(Shape.SQUARE, transform.applyTimes(PieceSpec(shape = Shape.TRIANGLE), 2).shape)
    }
}
