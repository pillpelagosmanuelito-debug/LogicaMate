package com.educalab.logicamate.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeductionEngineTest {

    private val people = listOf("Lía", "Tomás", "Nia")
    private val objects = listOf("cristal azul", "cristal verde", "llave dorada")

    @Test
    fun `enough negative clues yield a unique assignment`() {
        // Solución real: Lía=verde, Tomás=llave, Nia=azul.
        val clues = listOf(
            DeductionEngine.Clues.doesNotHave("Lía", "cristal azul"),
            DeductionEngine.Clues.doesNotHave("Lía", "llave dorada"),
            DeductionEngine.Clues.doesNotHave("Tomás", "cristal azul"),
            DeductionEngine.Clues.doesNotHave("Tomás", "cristal verde"),
        )
        val solution = DeductionEngine.uniqueSolution(people, objects, clues)
        assertEquals(mapOf("Lía" to "cristal verde", "Tomás" to "llave dorada", "Nia" to "cristal azul"), solution)
    }

    @Test
    fun `insufficient clues leave multiple valid assignments`() {
        val clues = listOf(DeductionEngine.Clues.doesNotHave("Lía", "cristal azul"))
        assertTrue(DeductionEngine.allValidAssignments(people, objects, clues).size > 1)
        assertNull(DeductionEngine.uniqueSolution(people, objects, clues))
    }

    @Test
    fun `contradictory clues yield zero valid assignments`() {
        val clues = listOf(
            DeductionEngine.Clues.has("Lía", "cristal azul"),
            DeductionEngine.Clues.doesNotHave("Lía", "cristal azul"),
        )
        assertTrue(DeductionEngine.allValidAssignments(people, objects, clues).isEmpty())
    }

    @Test
    fun `validateAnswer accepts only the true solution`() {
        val clues = listOf(
            DeductionEngine.Clues.has("Lía", "cristal verde"),
            DeductionEngine.Clues.has("Tomás", "llave dorada"),
        )
        val correct = mapOf("Lía" to "cristal verde", "Tomás" to "llave dorada", "Nia" to "cristal azul")
        val wrong = mapOf("Lía" to "cristal azul", "Tomás" to "cristal verde", "Nia" to "llave dorada")
        assertTrue(DeductionEngine.validateAnswer(people, objects, clues, correct))
        assertFalse(DeductionEngine.validateAnswer(people, objects, clues, wrong))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mismatched people and objects sizes are rejected`() {
        DeductionEngine.allValidAssignments(listOf("A", "B"), listOf("x", "y", "z"), emptyList())
    }

    @Test
    fun `four-person grid with full positive clues has a unique solution`() {
        val fourPeople = listOf("Lía", "Tomás", "Nia", "Kael")
        val fourObjects = listOf("a", "b", "c", "d")
        val clues = fourPeople.zip(fourObjects).map { (p, o) -> DeductionEngine.Clues.has(p, o) }
        assertEquals(fourPeople.zip(fourObjects).toMap(), DeductionEngine.uniqueSolution(fourPeople, fourObjects, clues))
    }
}
