package com.educalab.logicamate.domain.engine

/**
 * Motor de relaciones y restricciones de orden/posición (Cámara "Sala de
 * Relaciones"). Para el tamaño de problema usado en LogicaMate (3-5
 * elementos) la búsqueda exhaustiva sobre todas las permutaciones es simple,
 * rápida y —crucialmente— permite comprobar que la solución es ÚNICA, tal
 * como exige la sección 27 (validación de desafíos generados).
 */
object RelationEngine {

    sealed class Constraint {
        abstract fun holds(order: List<String>): Boolean

        data class Before(val a: String, val b: String) : Constraint() {
            override fun holds(order: List<String>) = order.indexOf(a) < order.indexOf(b)
        }

        data class ImmediatelyBefore(val a: String, val b: String) : Constraint() {
            override fun holds(order: List<String>) = order.indexOf(b) - order.indexOf(a) == 1
        }

        data class NotAdjacent(val a: String, val b: String) : Constraint() {
            override fun holds(order: List<String>) = kotlin.math.abs(order.indexOf(a) - order.indexOf(b)) != 1
        }

        data class AtPosition(val a: String, val position: Int) : Constraint() {
            override fun holds(order: List<String>) = order.indexOf(a) == position
        }
    }

    private fun <T> permutations(items: List<T>): Sequence<List<T>> = sequence {
        if (items.isEmpty()) {
            yield(emptyList())
        } else {
            for (i in items.indices) {
                val rest = items.toMutableList().also { it.removeAt(i) }
                for (p in permutations(rest)) yield(listOf(items[i]) + p)
            }
        }
    }

    /** Todas las permutaciones de [items] que satisfacen todas las [constraints]. */
    fun allValidOrderings(items: List<String>, constraints: List<Constraint>): List<List<String>> =
        permutations(items).filter { order -> constraints.all { it.holds(order) } }.toList()

    /**
     * Devuelve la solución si —y solo si— es única. Un conjunto de
     * restricciones que admite 0 o más de 1 solución no es un desafío válido
     * (ver DeductionAndRelationValidationTest).
     */
    fun uniqueSolution(items: List<String>, constraints: List<Constraint>): List<String>? {
        val solutions = allValidOrderings(items, constraints)
        return solutions.singleOrNull()
    }

    fun validateAnswer(items: List<String>, constraints: List<Constraint>, candidate: List<String>): Boolean =
        constraints.all { it.holds(candidate) } && candidate.toSet() == items.toSet() && candidate.size == items.size
}
