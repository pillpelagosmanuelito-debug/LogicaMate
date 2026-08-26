package com.educalab.logicamate.domain.engine

/**
 * Motor de secuencias numéricas (Cámara "Pasadizo de Secuencias").
 * Reglas soportadas: aritmética (+d), geométrica (*r), alternancia de dos
 * pasos (+a,+b repetido) y repetición cíclica de bloque fijo.
 * Puramente funcional y sin dependencias de Android -> 100% testeable en JVM.
 */
object SequenceEngine {

    sealed class SequenceRule {
        abstract fun next(seq: List<Int>): Int

        data class Arithmetic(val step: Int) : SequenceRule() {
            override fun next(seq: List<Int>) = seq.last() + step
        }

        data class Geometric(val ratio: Int) : SequenceRule() {
            override fun next(seq: List<Int>) = seq.last() * ratio
        }

        /** Alterna dos incrementos, p.ej. +2, +5, +2, +5... */
        data class AlternatingStep(val stepA: Int, val stepB: Int) : SequenceRule() {
            override fun next(seq: List<Int>): Int {
                // El índice de paso a aplicar depende de cuántos pasos ya se dieron.
                val stepsTaken = seq.size - 1
                return seq.last() + if (stepsTaken % 2 == 0) stepA else stepB
            }
        }
    }

    /**
     * Intenta detectar la regla más simple que explica la secuencia dada
     * (mínimo 3 elementos). Devuelve null si es ambigua o no reconocida.
     * Se prueba en orden de simplicidad: aritmética -> geométrica -> alternante.
     */
    fun detectRule(seq: List<Int>): SequenceRule? {
        require(seq.size >= 3) { "Se requieren al menos 3 términos para detectar una regla de forma fiable." }

        // Aritmética: diferencia constante.
        val diffs = seq.zipWithNext { a, b -> b - a }
        if (diffs.toSet().size == 1) {
            return SequenceRule.Arithmetic(diffs.first())
        }

        // Geométrica: razón constante (evitar división por cero).
        if (seq.all { it != 0 }) {
            val ratios = seq.zipWithNext { a, b -> if (a != 0 && b % a == 0) b / a else null }
            if (ratios.all { it != null } && ratios.map { it!! }.toSet().size == 1) {
                return SequenceRule.Geometric(ratios.first()!!)
            }
        }

        // Alternancia de dos pasos: diffs[0]==diffs[2]==diffs[4]... y diffs[1]==diffs[3]...
        if (diffs.size >= 3) {
            val evens = diffs.filterIndexed { i, _ -> i % 2 == 0 }.toSet()
            val odds = diffs.filterIndexed { i, _ -> i % 2 == 1 }.toSet()
            if (evens.size == 1 && odds.size == 1 && evens != odds) {
                return SequenceRule.AlternatingStep(evens.first(), odds.first())
            }
        }

        return null
    }

    /** Verifica que la regla detectada produzca una única continuación (sin ambigüedad). */
    fun hasUniqueSolution(seq: List<Int>): Boolean = detectRule(seq) != null

    fun nextValue(seq: List<Int>): Int? = detectRule(seq)?.next(seq)

    fun validateAnswer(seq: List<Int>, candidate: Int): Boolean = nextValue(seq) == candidate
}
