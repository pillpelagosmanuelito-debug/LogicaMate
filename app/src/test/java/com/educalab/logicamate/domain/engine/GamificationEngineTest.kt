package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.DifficultyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationEngineTest {

    @Test
    fun `advanced challenges award more xp than initial ones`() {
        val initial = GamificationEngine.xpForChallenge(DifficultyLevel.INITIAL, hintsUsed = 0, solvedOnFirstAttempt = true)
        val advanced = GamificationEngine.xpForChallenge(DifficultyLevel.ADVANCED, hintsUsed = 0, solvedOnFirstAttempt = true)
        assertTrue(advanced > initial)
    }

    @Test
    fun `using hints reduces xp but never below the floor`() {
        val noHints = GamificationEngine.xpForChallenge(DifficultyLevel.INITIAL, hintsUsed = 0, solvedOnFirstAttempt = false)
        val withHints = GamificationEngine.xpForChallenge(DifficultyLevel.INITIAL, hintsUsed = 3, solvedOnFirstAttempt = false)
        assertTrue(withHints < noHints)
        assertTrue(withHints >= 2)
    }

    @Test
    fun `first try bonus increases xp relative to a later attempt`() {
        val firstTry = GamificationEngine.xpForChallenge(DifficultyLevel.INTERMEDIATE, hintsUsed = 0, solvedOnFirstAttempt = true)
        val laterTry = GamificationEngine.xpForChallenge(DifficultyLevel.INTERMEDIATE, hintsUsed = 0, solvedOnFirstAttempt = false)
        assertTrue(firstTry > laterTry)
    }

    @Test
    fun `level 1 requires zero accumulated xp`() {
        assertEquals(1, GamificationEngine.levelForXp(0))
    }

    @Test
    fun `level increases as xp accumulates`() {
        val level10 = GamificationEngine.levelForXp(10)
        val level500 = GamificationEngine.levelForXp(500)
        assertTrue(level500 > level10)
    }

    @Test
    fun `xp into current level is non-negative and less than the span of that level`() {
        val xp = 275
        val level = GamificationEngine.levelForXp(xp)
        val into = GamificationEngine.xpIntoCurrentLevel(xp)
        val span = GamificationEngine.xpIntoCurrentLevel(xp) + GamificationEngine.xpNeededForNextLevel(xp)
        assertTrue(into >= 0)
        assertTrue(level >= 1)
        assertTrue(span > 0)
    }

    @Test
    fun `streak resets to 1 after a gap of more than one day`() {
        val today = 100L
        assertEquals(1, GamificationEngine.updateStreak(lastActiveDay = 95L, today = today, currentStreak = 6))
    }

    @Test
    fun `streak increments on the very next day`() {
        assertEquals(6, GamificationEngine.updateStreak(lastActiveDay = 99L, today = 100L, currentStreak = 5))
    }

    @Test
    fun `streak stays the same for repeated activity within the same day`() {
        assertEquals(3, GamificationEngine.updateStreak(lastActiveDay = 100L, today = 100L, currentStreak = 3))
    }

    @Test
    fun `first ever activity starts a streak of one`() {
        assertEquals(1, GamificationEngine.updateStreak(lastActiveDay = null, today = 50L, currentStreak = 0))
    }

    @Test
    fun `first mecanismo badge unlocks after a single solved challenge`() {
        val stats = GamificationEngine.UserStats(totalChallengesSolved = 1)
        val newBadges = GamificationEngine.evaluateNewBadges(stats, emptySet())
        assertTrue(GamificationEngine.BadgeCode.PRIMER_MECANISMO in newBadges)
    }

    @Test
    fun `already unlocked badges are not re-granted`() {
        val stats = GamificationEngine.UserStats(totalChallengesSolved = 5)
        val newBadges = GamificationEngine.evaluateNewBadges(stats, setOf(GamificationEngine.BadgeCode.PRIMER_MECANISMO))
        assertTrue(GamificationEngine.BadgeCode.PRIMER_MECANISMO !in newBadges)
    }

    @Test
    fun `maestro del templo badge requires all key fragments`() {
        val incomplete = GamificationEngine.UserStats(keyFragmentsCollected = 7, totalKeyFragments = 8)
        val complete = GamificationEngine.UserStats(keyFragmentsCollected = 8, totalKeyFragments = 8)
        assertTrue(GamificationEngine.BadgeCode.MAESTRO_DEL_TEMPLO !in GamificationEngine.evaluateNewBadges(incomplete, emptySet()))
        assertTrue(GamificationEngine.BadgeCode.MAESTRO_DEL_TEMPLO in GamificationEngine.evaluateNewBadges(complete, emptySet()))
    }

    @Test
    fun `dayIndex groups timestamps within the same day together`() {
        val morning = 10 * 3_600_000L
        val evening = 20 * 3_600_000L
        assertEquals(GamificationEngine.dayIndex(morning), GamificationEngine.dayIndex(evening))
    }
}
