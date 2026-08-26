package com.educalab.logicamate.domain.engine

import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressEngineTest {

    @Test
    fun `entrance chamber is always available regardless of previous status`() {
        val input = ProgressEngine.ChamberProgressInput(ChamberId.ENTRANCE, totalChallenges = 3, completedChallenges = 0, perfectChallenges = 0, previousChamberStatus = null)
        assertEquals(ChamberStatus.AVAILABLE, ProgressEngine.statusFor(input))
    }

    @Test
    fun `chamber stays locked until the previous one is completed`() {
        val input = ProgressEngine.ChamberProgressInput(ChamberId.SEQUENCES, totalChallenges = 10, completedChallenges = 0, perfectChallenges = 0, previousChamberStatus = ChamberStatus.STARTED)
        assertEquals(ChamberStatus.LOCKED, ProgressEngine.statusFor(input))
    }

    @Test
    fun `chamber becomes available once the previous one is completed`() {
        val input = ProgressEngine.ChamberProgressInput(ChamberId.SEQUENCES, totalChallenges = 10, completedChallenges = 0, perfectChallenges = 0, previousChamberStatus = ChamberStatus.COMPLETED)
        assertEquals(ChamberStatus.AVAILABLE, ProgressEngine.statusFor(input))
    }

    @Test
    fun `partial progress is reported as started`() {
        val input = ProgressEngine.ChamberProgressInput(ChamberId.PATTERNS, totalChallenges = 10, completedChallenges = 4, perfectChallenges = 1, previousChamberStatus = ChamberStatus.COMPLETED)
        assertEquals(ChamberStatus.STARTED, ProgressEngine.statusFor(input))
    }

    @Test
    fun `completing all challenges without a perfect run yields completed not mastered`() {
        val input = ProgressEngine.ChamberProgressInput(ChamberId.PATTERNS, totalChallenges = 10, completedChallenges = 10, perfectChallenges = 6, previousChamberStatus = ChamberStatus.COMPLETED)
        assertEquals(ChamberStatus.COMPLETED, ProgressEngine.statusFor(input))
    }

    @Test
    fun `perfect completion of every challenge yields mastered`() {
        val input = ProgressEngine.ChamberProgressInput(ChamberId.PATTERNS, totalChallenges = 10, completedChallenges = 10, perfectChallenges = 10, previousChamberStatus = ChamberStatus.COMPLETED)
        assertEquals(ChamberStatus.MASTERED, ProgressEngine.statusFor(input))
    }

    @Test
    fun `master chamber unlocks only when every other chamber is completed`() {
        val allButOne = ChamberId.entries.filter { it != ChamberId.ENTRANCE && it != ChamberId.MASTER }
            .associateWith { ChamberStatus.COMPLETED }
            .toMutableMap()
        assertTrue(ProgressEngine.isMasterChamberUnlocked(allButOne))
        allButOne[ChamberId.DEDUCTION] = ChamberStatus.STARTED
        assertFalse(ProgressEngine.isMasterChamberUnlocked(allButOne))
    }

    @Test
    fun `overall progress percent reflects completed over total across chambers`() {
        val inputs = listOf(
            ProgressEngine.ChamberProgressInput(ChamberId.PATTERNS, 10, 5, 0, ChamberStatus.COMPLETED),
            ProgressEngine.ChamberProgressInput(ChamberId.SEQUENCES, 10, 10, 10, ChamberStatus.COMPLETED),
        )
        assertEquals(75, ProgressEngine.overallProgressPercent(inputs))
    }

    @Test
    fun `overall progress percent is zero with no content`() {
        assertEquals(0, ProgressEngine.overallProgressPercent(emptyList()))
    }
}
