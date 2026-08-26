package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstructorEngineTest {

    @Test
    fun `star every three pieces is satisfied by a matching strip`() {
        val goal = ConstructorEngine.ConstructionGoal.ShapeEveryN(Shape.STAR, 3)
        val built = listOf(
            PieceSpec(shape = Shape.TRIANGLE), PieceSpec(shape = Shape.CIRCLE), PieceSpec(shape = Shape.STAR),
            PieceSpec(shape = Shape.SQUARE), PieceSpec(shape = Shape.HEXAGON), PieceSpec(shape = Shape.STAR),
        )
        assertTrue(ConstructorEngine.validate(built, goal))
    }

    @Test
    fun `star every three pieces rejects a star in the wrong position`() {
        val goal = ConstructorEngine.ConstructionGoal.ShapeEveryN(Shape.STAR, 3)
        val built = listOf(PieceSpec(shape = Shape.STAR), PieceSpec(shape = Shape.CIRCLE), PieceSpec(shape = Shape.SQUARE))
        assertFalse(ConstructorEngine.validate(built, goal))
    }

    @Test
    fun `alternate two shapes requires strict alternation`() {
        val goal = ConstructorEngine.ConstructionGoal.AlternateTwoShapes(Shape.TRIANGLE, Shape.CIRCLE)
        val good = listOf(Shape.TRIANGLE, Shape.CIRCLE, Shape.TRIANGLE, Shape.CIRCLE).map { PieceSpec(shape = it) }
        val bad = listOf(Shape.TRIANGLE, Shape.TRIANGLE, Shape.CIRCLE, Shape.CIRCLE).map { PieceSpec(shape = it) }
        assertTrue(ConstructorEngine.validate(good, goal))
        assertFalse(ConstructorEngine.validate(bad, goal))
    }

    @Test
    fun `ascending size cycle wraps back to small after large`() {
        val built = listOf(PieceSize.SMALL, PieceSize.MEDIUM, PieceSize.LARGE, PieceSize.SMALL).map { PieceSpec(size = it) }
        assertTrue(ConstructorEngine.validate(built, ConstructorEngine.ConstructionGoal.AscendingSizeCycle))
    }

    @Test
    fun `count increases by fixed step`() {
        val built = listOf(1, 3, 5, 7).map { PieceSpec(count = it) }
        assertTrue(ConstructorEngine.validate(built, ConstructorEngine.ConstructionGoal.CountIncreasesBy(2)))
    }

    @Test
    fun `count increases by rejects an inconsistent step`() {
        val built = listOf(1, 3, 4, 7).map { PieceSpec(count = it) }
        assertFalse(ConstructorEngine.validate(built, ConstructorEngine.ConstructionGoal.CountIncreasesBy(2)))
    }

    @Test
    fun `goals too short to evaluate are rejected rather than trivially accepted`() {
        val tooShort = listOf(PieceSpec(shape = Shape.TRIANGLE))
        assertFalse(ConstructorEngine.validate(tooShort, ConstructorEngine.ConstructionGoal.AlternateTwoShapes(Shape.TRIANGLE, Shape.CIRCLE)))
    }
}
