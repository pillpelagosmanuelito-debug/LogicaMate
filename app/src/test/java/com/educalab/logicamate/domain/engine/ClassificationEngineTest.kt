package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassificationEngineTest {

    private val pieces = listOf(
        PieceSpec(shape = Shape.TRIANGLE, color = PieceColor.GOLD),   // 0
        PieceSpec(shape = Shape.CIRCLE, color = PieceColor.CRYSTAL),  // 1
        PieceSpec(shape = Shape.TRIANGLE, color = PieceColor.CORAL),  // 2
        PieceSpec(shape = Shape.CIRCLE, color = PieceColor.STONE),    // 3
    )

    @Test
    fun `canonical partition groups by shape regardless of color`() {
        val partition = ClassificationEngine.canonicalPartition(pieces, ClassifyProperty.SHAPE)
        assertTrue(setOf(0, 2) in partition)
        assertTrue(setOf(1, 3) in partition)
        assertTrue(partition.size == 2)
    }

    @Test
    fun `validateGrouping accepts correct grouping regardless of label order`() {
        val childGroups = listOf(listOf(1, 3), listOf(0, 2)) // orden distinto al canónico, mismo contenido
        assertTrue(ClassificationEngine.validateGrouping(pieces, ClassifyProperty.SHAPE, childGroups))
    }

    @Test
    fun `validateGrouping rejects an incorrect grouping`() {
        val childGroups = listOf(listOf(0, 1), listOf(2, 3))
        assertFalse(ClassificationEngine.validateGrouping(pieces, ClassifyProperty.SHAPE, childGroups))
    }

    @Test
    fun `parity classification groups by even or odd count`() {
        val countPieces = (1..6).map { PieceSpec(count = it) }
        val partition = ClassificationEngine.canonicalPartition(countPieces, ClassifyProperty.PARITY_COUNT)
        assertTrue(setOf(0, 2, 4) in partition) // counts 1,3,5 -> odd
        assertTrue(setOf(1, 3, 5) in partition) // counts 2,4,6 -> even
    }

    @Test
    fun `well-formed check rejects singleton groups`() {
        val badSet = listOf(
            PieceSpec(shape = Shape.TRIANGLE), PieceSpec(shape = Shape.CIRCLE), PieceSpec(shape = Shape.SQUARE),
        )
        assertFalse(ClassificationEngine.isWellFormedForChallenge(badSet, ClassifyProperty.SHAPE))
    }

    @Test
    fun `well-formed check accepts balanced groups`() {
        assertTrue(ClassificationEngine.isWellFormedForChallenge(pieces, ClassifyProperty.SHAPE))
    }
}
