package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape

/**
 * Punto único donde la UI comprueba una respuesta. Cada categoría tiene una
 * forma natural distinta de "respuesta" (una pieza, una agrupación, un
 * orden, una asignación, una estructura libre) — este validador traduce esa
 * forma concreta a la comparación correcta sin que cada pantalla tenga que
 * conocer el formato de otras cámaras.
 */
object ChallengeValidator {

    /** Para Patrones, Secuencias, Analogías y Matrices: el usuario entrega una o más piezas. */
    fun validatePieceAnswer(challenge: Challenge, submitted: List<PieceSpec>): Boolean =
        submitted == challenge.solutionPieces

    /** Para Clasificación: el usuario entrega grupos de índices sobre challenge.optionPool. */
    fun validateClassification(challenge: Challenge, childGroups: List<List<Int>>): Boolean {
        val property = ClassifyProperty.valueOf(challenge.rule.params.getValue("property"))
        return ClassificationEngine.validateGrouping(challenge.optionPool, property, childGroups)
    }

    /** Para Relaciones: el usuario entrega un orden final de ids (mismos ids que rule.params["items"]). */
    fun validateRelationOrder(challenge: Challenge, submittedOrder: List<String>): Boolean {
        val items = challenge.rule.params.getValue("items").split(",")
        val expected = challenge.rule.params.getValue("solution").split(",")
        return submittedOrder == expected && submittedOrder.toSet() == items.toSet()
    }

    /** Para Deducción: el usuario entrega una asignación persona->objeto. */
    fun validateDeductionAssignment(challenge: Challenge, submitted: Map<String, String>): Boolean {
        val expectedEncoded = challenge.rule.params.getValue("solution")
        val expected = expectedEncoded.split(",").associate { pair ->
            val (k, v) = pair.split(":", limit = 2)
            k to v
        }
        return submitted == expected
    }

    /** Para el Taller Constructor: se reconstruye el objetivo desde rule.params y se valida la tira libre. */
    fun validateConstruction(challenge: Challenge, built: List<PieceSpec>): Boolean {
        val goal = decodeConstructionGoal(challenge.rule)
        return ConstructorEngine.validate(built, goal)
    }

    /** Visibilidad `internal` (no `private`) para permitir su reutilización desde
     *  SeedContentConsistencyTest, que valida el contenido semilla contra este mismo decodificador. */
    internal fun decodeConstructionGoal(rule: com.educalab.logicamate.domain.model.LogicRule): ConstructorEngine.ConstructionGoal =
        when (rule.type) {
            "SHAPE_EVERY_N" -> ConstructorEngine.ConstructionGoal.ShapeEveryN(
                shape = Shape.valueOf(rule.params.getValue("shape")),
                n = rule.params.getValue("n").toInt(),
            )
            "ALTERNATE_TWO_SHAPES" -> ConstructorEngine.ConstructionGoal.AlternateTwoShapes(
                shapeA = Shape.valueOf(rule.params.getValue("shapeA")),
                shapeB = Shape.valueOf(rule.params.getValue("shapeB")),
            )
            "ASCENDING_SIZE_CYCLE" -> ConstructorEngine.ConstructionGoal.AscendingSizeCycle
            "COUNT_INCREASES_BY" -> ConstructorEngine.ConstructionGoal.CountIncreasesBy(
                step = rule.params.getValue("step").toInt(),
            )
            else -> error("Tipo de objetivo de construcción desconocido: ${rule.type}")
        }

    /** Despacho genérico por categoría, usado por la pantalla de la Cámara Maestra (mezcla categorías). */
    fun validateAny(challenge: Challenge, submission: Submission): Boolean = when (challenge.category) {
        LogicCategory.PATTERN, LogicCategory.SEQUENCE, LogicCategory.ANALOGY, LogicCategory.MATRIX ->
            validatePieceAnswer(challenge, (submission as Submission.Pieces).pieces)
        LogicCategory.CLASSIFICATION -> validateClassification(challenge, (submission as Submission.Groups).groups)
        LogicCategory.RELATION -> validateRelationOrder(challenge, (submission as Submission.Order).order)
        LogicCategory.DEDUCTION -> validateDeductionAssignment(challenge, (submission as Submission.Assignment).map)
        LogicCategory.CONSTRUCTION -> validateConstruction(challenge, (submission as Submission.Pieces).pieces)
    }

    sealed class Submission {
        data class Pieces(val pieces: List<PieceSpec>) : Submission()
        data class Groups(val groups: List<List<Int>>) : Submission()
        data class Order(val order: List<String>) : Submission()
        data class Assignment(val map: Map<String, String>) : Submission()
    }
}
