package com.educalab.logicamate.seed

import com.educalab.logicamate.domain.engine.AnalogyEngine
import com.educalab.logicamate.domain.engine.ChallengeValidator
import com.educalab.logicamate.domain.engine.ClassificationEngine
import com.educalab.logicamate.domain.engine.ClassifyProperty
import com.educalab.logicamate.domain.engine.ConstructorEngine
import com.educalab.logicamate.domain.engine.MatrixEngine
import com.educalab.logicamate.domain.engine.PatternEngine
import com.educalab.logicamate.domain.engine.SequenceEngine
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.InteractionType
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.domain.model.PieceColor
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No sustituye a `tools/generate_seed_content.py` (que ya auto-verifica cada
 * desafío en Python antes de emitirlo), pero es la comprobación que
 * realmente importa: que los mismos motores Kotlin que usará la app en
 * tiempo de ejecución están de acuerdo con lo que quedó grabado en
 * SeedContent.kt. Si algún día alguien edita SeedContent.kt a mano (contra
 * la recomendación del propio archivo), esta prueba lo detectaría.
 */
class SeedContentConsistencyTest {

    private val all = SeedContent.all

    @Test
    fun `seed bank has at least 130 challenges`() {
        assertTrue(all.size >= 130)
    }

    @Test
    fun `every chamber has at least one seed challenge`() {
        ChamberId.entries.forEach { chamber ->
            assertTrue("La cámara $chamber no tiene desafíos", all.any { it.chamberId == chamber })
        }
    }

    @Test
    fun `option-select interaction never exceeds half of the seed content`() {
        val optionSelectCount = all.count { it.interactionType == InteractionType.OPTION_SELECT }
        assertTrue(optionSelectCount.toDouble() / all.size < 0.5)
    }

    @Test
    fun `every challenge exposes at most three hints and a non-blank explanation`() {
        all.forEach { challenge ->
            assertTrue(challenge.hints.size <= 3)
            assertTrue(challenge.explanation.isNotBlank())
        }
    }

    @Test
    fun `pattern-cycle seed challenges are solved by PatternEngine`() {
        all.filter { it.category == LogicCategory.PATTERN && it.rule.type == "PATTERN_CYCLE" }
            .forEach { c ->
                val shown = c.items.dropLast(1) // quita el BLANK final
                val computed = PatternEngine.nextInCycle(shown)
                assertEquals("Fallo en ${c.id}", c.solutionPieces.single(), computed)
            }
    }

    @Test
    fun `pattern-multi-property seed challenges are solved by PatternEngine`() {
        all.filter { it.rule.type == "PATTERN_MULTI_PROPERTY" }.forEach { c ->
            val shown = c.items.dropLast(1)
            val computed = PatternEngine.nextMultiProperty(shown)
            assertEquals("Fallo en ${c.id}", c.solutionPieces.single(), computed)
        }
    }

    @Test
    fun `sequence seed challenges are solved by SequenceEngine`() {
        all.filter { it.category == LogicCategory.SEQUENCE }.forEach { c ->
            val values = c.items.dropLast(1).map { it.value!! }
            val computed = SequenceEngine.nextValue(values)
            assertEquals("Fallo en ${c.id}", c.solutionPieces.single().value, computed)
        }
    }

    @Test
    fun `analogy seed challenges are solved by AnalogyEngine`() {
        all.filter { it.category == LogicCategory.ANALOGY }.forEach { c ->
            val (a, aPrime, b, _) = c.items
            val transform = decodeTransform(c.rule.params)
            assertTrue("Fallo en ${c.id}", AnalogyEngine.validateAnswer(a, aPrime, transform, b, c.solutionPieces.single()))
        }
    }

    @Test
    fun `matrix seed challenges are solved by MatrixEngine`() {
        all.filter { it.category == LogicCategory.MATRIX }.forEach { c ->
            val cols = c.rule.params.getValue("cols").toInt()
            val transform = decodeTransform(c.rule.params)
            val blankFlatIndex = c.items.indexOfFirst { it.isBlank }
            val row = blankFlatIndex / cols
            val col = blankFlatIndex % cols
            val baseRow = c.items.subList(0, cols) // fila 0 nunca contiene el hueco (garantizado por el generador)
            val computed = MatrixEngine.solveCell(baseRow, transform, row, col)
            assertEquals("Fallo en ${c.id}", c.solutionPieces.single(), computed)
        }
    }

    @Test
    fun `classification seed challenges match ClassificationEngine's canonical partition`() {
        all.filter { it.category == LogicCategory.CLASSIFICATION }.forEach { c ->
            val property = ClassifyProperty.valueOf(c.rule.params.getValue("property"))
            val computedPartition = ClassificationEngine.canonicalPartition(c.optionPool, property)
            val storedPartition = c.rule.params.getValue("solution")
                .split("|").map { group -> group.split(",").map { it.toInt() }.toSet() }.toSet()
            assertEquals("Fallo en ${c.id}", storedPartition, computedPartition)
        }
    }

    @Test
    fun `relation seed challenges store a solution that is a true permutation of its items`() {
        all.filter { it.category == LogicCategory.RELATION }.forEach { c ->
            val items = c.rule.params.getValue("items").split(",")
            val solution = c.rule.params.getValue("solution").split(",")
            assertEquals("Fallo en ${c.id}", items.toSet(), solution.toSet())
            assertEquals("Fallo en ${c.id}", items.size, solution.size)
        }
    }

    @Test
    fun `deduction seed challenges store a solution that is a true bijection`() {
        all.filter { it.category == LogicCategory.DEDUCTION }.forEach { c ->
            val people = c.rule.params.getValue("people").split(",")
            val objects = c.rule.params.getValue("objects").split(",")
            val solution = c.rule.params.getValue("solution").split(",").associate {
                val (p, o) = it.split(":", limit = 2)
                p to o
            }
            assertEquals("Fallo en ${c.id}", people.toSet(), solution.keys)
            assertEquals("Fallo en ${c.id}", objects.toSet(), solution.values.toSet())
        }
    }

    @Test
    fun `construction seed challenges decode to a valid goal and a non-empty palette`() {
        all.filter { it.category == LogicCategory.CONSTRUCTION }.forEach { c ->
            val goal = ChallengeValidator.decodeConstructionGoal(c.rule) // no debe lanzar excepción
            assertTrue("Fallo en ${c.id}", c.optionPool.isNotEmpty())
            assertTrue(goal.description.isNotBlank())
        }
    }

    private fun decodeTransform(params: Map<String, String>): MatrixEngine.CellTransform = when (params.getValue("kind")) {
        "MULTIPLY_COUNT" -> MatrixEngine.CellTransform.MultiplyCount(params.getValue("factor").toInt())
        "CYCLE_SIZE" -> MatrixEngine.CellTransform.CycleSize()
        "CYCLE_COLOR" -> MatrixEngine.CellTransform.CycleColor(params.getValue("order").split(",").map { PieceColor.valueOf(it) })
        "CYCLE_SHAPE" -> MatrixEngine.CellTransform.CycleShape(params.getValue("order").split(",").map { Shape.valueOf(it) })
        else -> error("Tipo de transformación desconocido")
    }
}
