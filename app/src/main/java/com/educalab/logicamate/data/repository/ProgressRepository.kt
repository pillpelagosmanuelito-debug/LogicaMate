package com.educalab.logicamate.data.repository

import com.educalab.logicamate.data.local.LogicaMateDatabase
import com.educalab.logicamate.data.local.entity.AttemptEntity
import com.educalab.logicamate.data.local.entity.HintUsageEntity
import com.educalab.logicamate.data.local.entity.KeyFragmentEntity
import com.educalab.logicamate.data.local.entity.ProgressEntity
import com.educalab.logicamate.data.local.entity.UnlockedFragmentEntity
import com.educalab.logicamate.data.local.entity.UserBadgeEntity
import com.educalab.logicamate.data.local.entity.UserStatsEntity
import com.educalab.logicamate.domain.engine.GamificationEngine
import com.educalab.logicamate.domain.engine.ProgressEngine
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import com.educalab.logicamate.domain.model.DifficultyLevel

data class AttemptResult(
    val xpAwarded: Int,
    val newTotalXp: Int,
    val newStreak: Int,
    val newChamberStatus: ChamberStatus,
    val fragmentUnlocked: Boolean,
    val newlyUnlockedBadges: List<GamificationEngine.BadgeCode>,
)

class ProgressRepository(private val db: LogicaMateDatabase, private val userProfileId: Long = 1L) {

    /**
     * Registra el resultado de un intento sobre un desafío y actualiza en
     * cascada: XP, racha, progreso de la cámara y, si corresponde,
     * fragmento de la Llave Lógica + insignias nuevas. Devuelve un resumen
     * apto para animar la UI (barra de XP, fragmento, insignia...).
     */
    suspend fun recordAttempt(
        challengeId: String,
        chamberId: ChamberId,
        difficulty: DifficultyLevel,
        isCorrect: Boolean,
        hintsUsedLevels: List<Int>,
        submittedSolutionEncoded: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): AttemptResult {
        val priorAttempts = db.attemptDao().countForChallenge(challengeId)
        val attemptNumber = priorAttempts + 1

        val attemptId = db.attemptDao().insert(
            AttemptEntity(
                challengeId = challengeId,
                userProfileId = userProfileId,
                startedAtMillis = nowMillis,
                endedAtMillis = nowMillis,
                isCorrect = isCorrect,
                attemptNumber = attemptNumber,
                hintsUsedCount = hintsUsedLevels.size,
                submittedSolutionEncoded = submittedSolutionEncoded,
            ),
        )
        hintsUsedLevels.forEach { level ->
            db.attemptDao().insertHintUsage(HintUsageEntity(attemptId = attemptId, hintLevel = level, usedAtMillis = nowMillis))
        }

        if (!isCorrect) {
            // Un intento incorrecto no otorga XP ni progreso, pero queda persistido para el historial.
            val stats = currentStats()
            return AttemptResult(0, stats.totalXp, stats.currentStreak, currentChamberStatus(chamberId), false, emptyList())
        }

        val xpAwarded = GamificationEngine.xpForChallenge(
            difficulty = difficulty,
            hintsUsed = hintsUsedLevels.size,
            solvedOnFirstAttempt = attemptNumber == 1,
        )

        val statsBefore = currentStats()
        val today = GamificationEngine.dayIndex(nowMillis)
        val newStreak = GamificationEngine.updateStreak(statsBefore.lastActiveDay, today, statsBefore.currentStreak)
        val newTotalXp = statsBefore.totalXp + xpAwarded
        db.gamificationDao().upsertStats(
            UserStatsEntity(userProfileId = userProfileId, totalXp = newTotalXp, currentStreak = newStreak, lastActiveDay = today),
        )

        val newChamberStatus = updateChamberProgress(chamberId, xpAwarded, wasFirstTryNoHints = attemptNumber == 1 && hintsUsedLevels.isEmpty())
        val fragmentUnlocked = maybeUnlockKeyFragment(chamberId, newChamberStatus, nowMillis)
        val newBadges = evaluateAndUnlockBadges(nowMillis)

        return AttemptResult(xpAwarded, newTotalXp, newStreak, newChamberStatus, fragmentUnlocked, newBadges)
    }

    private suspend fun currentStats(): UserStatsEntity =
        db.gamificationDao().getStats(userProfileId)
            ?: UserStatsEntity(userProfileId, 0, 0, null) // defensivo: no debería ocurrir tras el seeding

    private suspend fun currentChamberStatus(chamberId: ChamberId): ChamberStatus {
        val progress = db.progressDao().get(chamberId.name, userProfileId) ?: return ChamberStatus.LOCKED
        return ChamberStatus.valueOf(progress.status)
    }

    private suspend fun updateChamberProgress(chamberId: ChamberId, xpAwarded: Int, wasFirstTryNoHints: Boolean): ChamberStatus {
        val progress = db.progressDao().get(chamberId.name, userProfileId)
            ?: ProgressEntity(userProfileId, chamberId.name, ChamberStatus.AVAILABLE.name, 0, 0, db.challengeDao().countByChamber(chamberId.name), 0)

        val newCompleted = (progress.challengesCompleted + 1).coerceAtMost(progress.totalChallenges)
        val newPerfect = if (wasFirstTryNoHints) (progress.perfectChallenges + 1).coerceAtMost(progress.totalChallenges) else progress.perfectChallenges

        val previousChamber = ChamberId.entries.firstOrNull { it.order == chamberId.order - 1 }
        val previousStatus = previousChamber?.let { currentChamberStatus(it) }

        val newStatus = ProgressEngine.statusFor(
            ProgressEngine.ChamberProgressInput(
                chamberId = chamberId,
                totalChallenges = progress.totalChallenges,
                completedChallenges = newCompleted,
                perfectChallenges = newPerfect,
                previousChamberStatus = previousStatus,
            ),
        )

        db.progressDao().upsert(
            progress.copy(
                status = newStatus.name,
                challengesCompleted = newCompleted,
                perfectChallenges = newPerfect,
                xpEarnedInChamber = progress.xpEarnedInChamber + xpAwarded,
            ),
        )

        // Desbloquear la siguiente cámara en el mapa si esta se completó.
        if (newStatus == ChamberStatus.COMPLETED || newStatus == ChamberStatus.MASTERED) {
            val next = ChamberId.entries.firstOrNull { it.order == chamberId.order + 1 }
            if (next != null) {
                val nextProgress = db.progressDao().get(next.name, userProfileId)
                if (nextProgress != null && ChamberStatus.valueOf(nextProgress.status) == ChamberStatus.LOCKED) {
                    db.progressDao().upsert(nextProgress.copy(status = ChamberStatus.AVAILABLE.name))
                }
            }
        }
        return newStatus
    }

    // Debe coincidir exactamente con las cámaras sembradas en DatabaseSeeder.seedKeyFragments().
    private val fragmentGrantingChambers = setOf(
        ChamberId.PATTERNS, ChamberId.SEQUENCES, ChamberId.ANALOGIES, ChamberId.CLASSIFICATION,
        ChamberId.MATRICES, ChamberId.RELATIONS, ChamberId.DEDUCTION, ChamberId.CONSTRUCTOR,
    )

    private suspend fun maybeUnlockKeyFragment(chamberId: ChamberId, newStatus: ChamberStatus, nowMillis: Long): Boolean {
        if (newStatus != ChamberStatus.COMPLETED && newStatus != ChamberStatus.MASTERED) return false
        if (chamberId !in fragmentGrantingChambers) return false
        val fragmentId = "FRAG_${chamberId.name}"
        val alreadyUnlocked = db.keyFragmentDao().getUnlockedIds(userProfileId).contains(fragmentId)
        if (alreadyUnlocked) return false
        db.keyFragmentDao().unlock(UnlockedFragmentEntity(userProfileId = userProfileId, keyFragmentId = fragmentId, unlockedAtMillis = nowMillis))
        return true
    }

    private suspend fun evaluateAndUnlockBadges(nowMillis: Long): List<GamificationEngine.BadgeCode> {
        val stats = GamificationEngine.UserStats(
            patternsSolved = db.attemptDao().countCorrectByCategory(userProfileId, "PATTERN"),
            sequencesSolved = db.attemptDao().countCorrectByCategory(userProfileId, "SEQUENCE"),
            relationsSolved = db.attemptDao().countCorrectByCategory(userProfileId, "RELATION"),
            matricesSolved = db.attemptDao().countCorrectByCategory(userProfileId, "MATRIX"),
            deductionsSolved = db.attemptDao().countCorrectByCategory(userProfileId, "DEDUCTION"),
            constructionsSolved = db.attemptDao().countCorrectByCategory(userProfileId, "CONSTRUCTION"),
            totalChallengesSolved = db.attemptDao().countCorrectForUser(userProfileId),
            keyFragmentsCollected = db.keyFragmentDao().getUnlockedIds(userProfileId).size,
            totalKeyFragments = 8,
        )
        val already = db.gamificationDao().getUnlockedBadgeIds(userProfileId).map { GamificationEngine.BadgeCode.valueOf(it) }.toSet()
        val newBadges = GamificationEngine.evaluateNewBadges(stats, already)
        newBadges.forEach { code ->
            db.gamificationDao().unlockBadge(UserBadgeEntity(userProfileId = userProfileId, badgeId = code.name, unlockedAtMillis = nowMillis))
        }
        return newBadges
    }
}
