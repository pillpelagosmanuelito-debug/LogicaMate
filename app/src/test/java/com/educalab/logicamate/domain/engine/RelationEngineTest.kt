package com.educalab.logicamate.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelationEngineTest {

    @Test
    fun `full precedence chain has a unique solution`() {
        val items = listOf("A", "B", "C")
        val constraints = listOf(
            RelationEngine.Constraint.Before("A", "B"),
            RelationEngine.Constraint.Before("B", "C"),
        )
        assertEquals(listOf("A", "B", "C"), RelationEngine.uniqueSolution(items, constraints))
    }

    @Test
    fun `underconstrained relation has more than one valid ordering`() {
        val items = listOf("A", "B", "C")
        val constraints = listOf(RelationEngine.Constraint.Before("A", "B"))
        assertTrue(RelationEngine.allValidOrderings(items, constraints).size > 1)
        assertNull(RelationEngine.uniqueSolution(items, constraints))
    }

    @Test
    fun `overconstrained relation has zero valid orderings`() {
        val items = listOf("A", "B")
        val constraints = listOf(
            RelationEngine.Constraint.Before("A", "B"),
            RelationEngine.Constraint.Before("B", "A"),
        )
        assertTrue(RelationEngine.allValidOrderings(items, constraints).isEmpty())
        assertNull(RelationEngine.uniqueSolution(items, constraints))
    }

    @Test
    fun `not-adjacent constraint filters orderings correctly`() {
        val items = listOf("A", "B", "C")
        val constraints = listOf(RelationEngine.Constraint.NotAdjacent("A", "C"))
        val valid = RelationEngine.allValidOrderings(items, constraints)
        assertTrue(valid.all { kotlin.math.abs(it.indexOf("A") - it.indexOf("C")) != 1 })
    }

    @Test
    fun `immediately-before constraint requires exact adjacency in order`() {
        val items = listOf("A", "B", "C")
        val constraints = listOf(RelationEngine.Constraint.ImmediatelyBefore("A", "B"))
        val valid = RelationEngine.allValidOrderings(items, constraints)
        assertTrue(valid.all { it.indexOf("B") - it.indexOf("A") == 1 })
    }

    @Test
    fun `validateAnswer rejects an order missing an item`() {
        val items = listOf("A", "B", "C")
        val constraints = listOf(RelationEngine.Constraint.Before("A", "B"))
        assertFalse(RelationEngine.validateAnswer(items, constraints, listOf("A", "B")))
    }

    @Test
    fun `four element chain with extra redundant clue stays unique`() {
        val items = listOf("A", "B", "C", "D")
        val constraints = listOf(
            RelationEngine.Constraint.Before("A", "B"),
            RelationEngine.Constraint.Before("B", "C"),
            RelationEngine.Constraint.Before("C", "D"),
            RelationEngine.Constraint.NotAdjacent("A", "D"),
        )
        assertEquals(listOf("A", "B", "C", "D"), RelationEngine.uniqueSolution(items, constraints))
    }
}
