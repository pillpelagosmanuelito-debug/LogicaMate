package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.DifficultyLevel

/**
 * Reglas de XP, nivel, racha e insignias. Usa Long en milisegundos (no
 * java.time, para evitar requerir core-library-desugaring con minSdk 24) y
 * convierte a "día" mediante división entera — suficiente para una racha
 * diaria local sin sincronización horaria estricta.
 */
object GamificationEngine {

    private const val DAY_MS = 86_400_000L

    fun dayIndex(epochMillis: Long): Long = epochMillis / DAY_MS

    /** XP base por dificultad, con penalización moderada por pista usada y bono por primer intento. */
    fun xpForChallenge(difficulty: DifficultyLevel, hintsUsed: Int, solvedOnFirstAttempt: Boolean): Int {
        val base = when (difficulty) {
            DifficultyLevel.INITIAL -> 10
            DifficultyLevel.INTERMEDIATE -> 20
            DifficultyLevel.ADVANCED -> 35
        }
        val hintPenalty = (hintsUsed.coerceIn(0, 3)) * 3
        val firstTryBonus = if (solvedOnFirstAttempt) 5 else 0
        return (base - hintPenalty + firstTryBonus).coerceAtLeast(2)
    }

    /** Umbrales de nivel: crecimiento suave (nivel n requiere 50*n(n+1)/2 XP acumulado aprox triangular). */
    private fun xpRequiredForLevel(level: Int): Int {
        if (level <= 1) return 0
        return 50 * (level - 1) * level / 2
    }

    fun levelForXp(totalXp: Int): Int {
        var level = 1
        while (xpRequiredForLevel(level + 1) <= totalXp) level++
        return level
    }

    fun xpIntoCurrentLevel(totalXp: Int): Int = totalXp - xpRequiredForLevel(levelForXp(totalXp))

    fun xpNeededForNextLevel(totalXp: Int): Int {
        val next = levelForXp(totalXp) + 1
        return xpRequiredForLevel(next) - xpRequiredForLevel(levelForXp(totalXp))
    }

    /**
     * Nueva racha dado el último día de actividad y el día actual.
     * - Mismo día -> racha sin cambios (ya contabilizado).
     * - Día siguiente consecutivo -> racha + 1.
     * - Cualquier salto mayor -> racha reinicia a 1.
     * - Sin actividad previa -> racha 1.
     */
    fun updateStreak(lastActiveDay: Long?, today: Long, currentStreak: Int): Int = when {
        lastActiveDay == null -> 1
        lastActiveDay == today -> currentStreak.coerceAtLeast(1)
        lastActiveDay == today - 1 -> currentStreak + 1
        else -> 1
    }

    data class UserStats(
        val chambersCompleted: Set<String> = emptySet(),
        val patternsSolved: Int = 0,
        val sequencesSolved: Int = 0,
        val relationsSolved: Int = 0,
        val matricesSolved: Int = 0,
        val deductionsSolved: Int = 0,
        val constructionsSolved: Int = 0,
        val totalChallengesSolved: Int = 0,
        val keyFragmentsCollected: Int = 0,
        val totalKeyFragments: Int = 0,
        val strategiesUsedAcrossHints: Int = 0,
    )

    enum class BadgeCode {
        PRIMER_MECANISMO,
        CAZADOR_DE_PATRONES,
        MAESTRO_DE_SECUENCIAS,
        DETECTIVE_DE_RELACIONES,
        CONSTRUCTOR_LOGICO,
        EXPLORADOR_DE_MATRICES,
        GRAN_DEDUCIDOR,
        MAESTRO_DEL_TEMPLO,
    }

    /** Evalúa qué insignias (aún no obtenidas) corresponde otorgar dado el estado actual. */
    fun evaluateNewBadges(stats: UserStats, alreadyUnlocked: Set<BadgeCode>): List<BadgeCode> {
        val unlocked = mutableListOf<BadgeCode>()
        fun grant(code: BadgeCode, condition: Boolean) {
            if (condition && code !in alreadyUnlocked) unlocked += code
        }
        grant(BadgeCode.PRIMER_MECANISMO, stats.totalChallengesSolved >= 1)
        grant(BadgeCode.CAZADOR_DE_PATRONES, stats.patternsSolved >= 12)
        grant(BadgeCode.MAESTRO_DE_SECUENCIAS, stats.sequencesSolved >= 12)
        grant(BadgeCode.DETECTIVE_DE_RELACIONES, stats.relationsSolved >= 8)
        grant(BadgeCode.CONSTRUCTOR_LOGICO, stats.constructionsSolved >= 6)
        grant(BadgeCode.EXPLORADOR_DE_MATRICES, stats.matricesSolved >= 10)
        grant(BadgeCode.GRAN_DEDUCIDOR, stats.deductionsSolved >= 8)
        grant(
            BadgeCode.MAESTRO_DEL_TEMPLO,
            stats.keyFragmentsCollected >= stats.totalKeyFragments && stats.totalKeyFragments > 0,
        )
        return unlocked
    }
}
