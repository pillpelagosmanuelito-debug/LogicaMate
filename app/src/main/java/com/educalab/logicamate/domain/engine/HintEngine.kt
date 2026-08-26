package com.educalab.logicamate.domain.engine

/**
 * Política de pistas (sección 22-23): tres niveles progresivos que enseñan
 * estrategia, nunca la respuesta directa. Los niveles deben revelarse en
 * orden — no se puede saltar a la Pista 3 sin haber visto la 1 y la 2.
 */
object HintEngine {

    const val MAX_HINT_LEVEL = 3

    fun canReveal(requestedLevel: Int, alreadyRevealedLevels: Set<Int>): Boolean {
        if (requestedLevel !in 1..MAX_HINT_LEVEL) return false
        if (requestedLevel in alreadyRevealedLevels) return true // re-mostrar una ya vista es válido
        val previousLevelsRevealed = (1 until requestedLevel).all { it in alreadyRevealedLevels }
        return previousLevelsRevealed
    }

    fun nextRevealableLevel(alreadyRevealedLevels: Set<Int>): Int? {
        val next = (1..MAX_HINT_LEVEL).firstOrNull { it !in alreadyRevealedLevels }
        return next
    }
}
