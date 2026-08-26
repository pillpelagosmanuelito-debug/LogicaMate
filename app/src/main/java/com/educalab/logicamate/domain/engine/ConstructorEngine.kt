package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.PieceSize
import com.educalab.logicamate.domain.model.PieceSpec
import com.educalab.logicamate.domain.model.Shape

/**
 * Motor del Taller Constructor (Cámara 9): el niño construye libremente una
 * tira de piezas y la aplicación comprueba si cumple una condición dada en
 * lenguaje natural, p.ej. "la estrella aparece cada tres piezas" o "alterna
 * dos formas". No es una selección múltiple: valida la ESTRUCTURA construida.
 */
object ConstructorEngine {

    sealed class ConstructionGoal {
        abstract fun isSatisfiedBy(built: List<PieceSpec>): Boolean
        abstract val description: String

        /** Una forma concreta debe aparecer exactamente cada [n] posiciones (1-indexado). */
        data class ShapeEveryN(val shape: Shape, val n: Int) : ConstructionGoal() {
            override val description = "La pieza ${shape.name} debe aparecer cada $n piezas."
            override fun isSatisfiedBy(built: List<PieceSpec>): Boolean {
                if (built.size < n) return false
                return built.indices.all { i ->
                    val shouldBeTarget = (i + 1) % n == 0
                    (built[i].shape == shape) == shouldBeTarget
                }
            }
        }

        /** La tira debe alternar estrictamente entre exactamente dos formas distintas. */
        data class AlternateTwoShapes(val shapeA: Shape, val shapeB: Shape) : ConstructionGoal() {
            override val description = "Alterna estrictamente ${shapeA.name} y ${shapeB.name}."
            override fun isSatisfiedBy(built: List<PieceSpec>): Boolean {
                if (built.size < 4) return false
                return built.indices.all { i ->
                    val expected = if (i % 2 == 0) shapeA else shapeB
                    built[i].shape == expected
                }
            }
        }

        /** El tamaño debe ser estrictamente creciente en ciclos (small->medium->large->small...). */
        object AscendingSizeCycle : ConstructionGoal() {
            override val description = "El tamaño debe crecer en ciclos: pequeño, mediano, grande, pequeño..."
            private val order = listOf(PieceSize.SMALL, PieceSize.MEDIUM, PieceSize.LARGE)
            override fun isSatisfiedBy(built: List<PieceSpec>): Boolean {
                if (built.size < 3) return false
                return built.indices.all { i -> built[i].size == order[i % order.size] }
            }
        }

        /** La cantidad de cada pieza debe aumentar en un paso fijo respecto a la anterior. */
        data class CountIncreasesBy(val step: Int) : ConstructionGoal() {
            override val description = "La cantidad debe aumentar de $step en $step en cada pieza."
            override fun isSatisfiedBy(built: List<PieceSpec>): Boolean {
                if (built.size < 3) return false
                return built.zipWithNext().all { (a, b) -> b.count - a.count == step }
            }
        }
    }

    fun validate(built: List<PieceSpec>, goal: ConstructionGoal): Boolean = goal.isSatisfiedBy(built)
}
