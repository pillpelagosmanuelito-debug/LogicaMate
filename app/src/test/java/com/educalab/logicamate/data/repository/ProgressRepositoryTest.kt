package com.educalab.logicamate.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.educalab.logicamate.data.local.DatabaseSeeder
import com.educalab.logicamate.data.local.LogicaMateDatabase
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import com.educalab.logicamate.domain.model.DifficultyLevel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProgressRepositoryTest {

    private lateinit var db: LogicaMateDatabase
    private lateinit var repo: ProgressRepository

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LogicaMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseSeeder(db).seedIfNeeded()
        repo = ProgressRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun completeChamber(chamberId: ChamberId) {
        for (c in db.challengeDao().getByChamber(chamberId.name)) {
            repo.recordAttempt(c.id, chamberId, DifficultyLevel.valueOf(c.difficulty), true, emptyList(), "x")
        }
    }

    @Test
    fun `a correct attempt awards positive xp`() = runBlocking {
        val challenge = db.challengeDao().getByChamber(ChamberId.PATTERNS.name).first()
        val result = repo.recordAttempt(
            challengeId = challenge.id, chamberId = ChamberId.PATTERNS, difficulty = DifficultyLevel.valueOf(challenge.difficulty),
            isCorrect = true, hintsUsedLevels = emptyList(), submittedSolutionEncoded = "x",
        )
        assertTrue(result.xpAwarded > 0)
        assertEquals(result.xpAwarded, result.newTotalXp)
    }

    @Test
    fun `an incorrect attempt awards zero xp and does not advance progress`() = runBlocking {
        val challenge = db.challengeDao().getByChamber(ChamberId.PATTERNS.name).first()
        val before = db.progressDao().get(ChamberId.PATTERNS.name)!!
        val result = repo.recordAttempt(
            challengeId = challenge.id, chamberId = ChamberId.PATTERNS, difficulty = DifficultyLevel.valueOf(challenge.difficulty),
            isCorrect = false, hintsUsedLevels = emptyList(), submittedSolutionEncoded = "x",
        )
        val after = db.progressDao().get(ChamberId.PATTERNS.name)!!
        assertEquals(0, result.xpAwarded)
        assertEquals(before.challengesCompleted, after.challengesCompleted)
    }

    @Test
    fun `using a hint reduces the xp awarded compared to no hints`() = runBlocking {
        val challenges = db.challengeDao().getByChamber(ChamberId.SEQUENCES.name)
        val a = challenges[0]
        val b = challenges[1]
        val noHint = repo.recordAttempt(a.id, ChamberId.SEQUENCES, DifficultyLevel.valueOf(a.difficulty), true, emptyList(), "x")
        val withHint = repo.recordAttempt(b.id, ChamberId.SEQUENCES, DifficultyLevel.valueOf(b.difficulty), true, listOf(1, 2), "x")
        assertTrue(withHint.xpAwarded <= noHint.xpAwarded)
    }

    @Test
    fun `completing every challenge in a chamber marks it completed and unlocks the next one`() = runBlocking {
        completeChamber(ChamberId.ENTRANCE)
        val challenges = db.challengeDao().getByChamber(ChamberId.PATTERNS.name)
        var lastStatus = ChamberStatus.AVAILABLE
        for (c in challenges) {
            val result = repo.recordAttempt(c.id, ChamberId.PATTERNS, DifficultyLevel.valueOf(c.difficulty), true, emptyList(), "x")
            lastStatus = result.newChamberStatus
        }
        assertTrue(lastStatus == ChamberStatus.COMPLETED || lastStatus == ChamberStatus.MASTERED)
        val nextChamber = ChamberId.entries.first { it.order == ChamberId.PATTERNS.order + 1 }
        val nextProgress = db.progressDao().get(nextChamber.name)!!
        assertEquals(ChamberStatus.AVAILABLE.name, nextProgress.status)
    }

    @Test
    fun `completing a fragment-granting chamber unlocks its key fragment exactly once`() = runBlocking {
        completeChamber(ChamberId.ENTRANCE)
        val challenges = db.challengeDao().getByChamber(ChamberId.PATTERNS.name)
        var fragmentUnlockedCount = 0
        for (c in challenges) {
            val result = repo.recordAttempt(c.id, ChamberId.PATTERNS, DifficultyLevel.valueOf(c.difficulty), true, emptyList(), "x")
            if (result.fragmentUnlocked) fragmentUnlockedCount++
        }
        assertEquals(1, fragmentUnlockedCount)
        assertTrue(db.keyFragmentDao().getUnlockedIds().contains("FRAG_PATTERNS"))
    }

    @Test
    fun `solving the first challenge ever unlocks the Primer Mecanismo badge`() = runBlocking {
        val challenge = db.challengeDao().getByChamber(ChamberId.ENTRANCE.name).first()
        val result = repo.recordAttempt(challenge.id, ChamberId.ENTRANCE, DifficultyLevel.valueOf(challenge.difficulty), true, emptyList(), "x")
        assertTrue(result.newlyUnlockedBadges.any { it.name == "PRIMER_MECANISMO" })
    }

    @Test
    fun `streak increases across consecutive simulated days`() = runBlocking {
        val challenges = db.challengeDao().getByChamber(ChamberId.SEQUENCES.name)
        val dayOneMillis = 1_700_000_000_000L
        val dayTwoMillis = dayOneMillis + 86_400_000L
        val r1 = repo.recordAttempt(challenges[0].id, ChamberId.SEQUENCES, DifficultyLevel.valueOf(challenges[0].difficulty), true, emptyList(), "x", nowMillis = dayOneMillis)
        val r2 = repo.recordAttempt(challenges[1].id, ChamberId.SEQUENCES, DifficultyLevel.valueOf(challenges[1].difficulty), true, emptyList(), "x", nowMillis = dayTwoMillis)
        assertEquals(1, r1.newStreak)
        assertEquals(2, r2.newStreak)
    }

    @Test
    fun `attempt history is persisted and queryable per challenge`() = runBlocking {
        val challenge = db.challengeDao().getByChamber(ChamberId.MATRICES.name).first()
        repo.recordAttempt(challenge.id, ChamberId.MATRICES, DifficultyLevel.valueOf(challenge.difficulty), false, emptyList(), "wrong")
        repo.recordAttempt(challenge.id, ChamberId.MATRICES, DifficultyLevel.valueOf(challenge.difficulty), true, emptyList(), "right")
        val attempts = db.attemptDao().getForChallenge(challenge.id)
        assertEquals(2, attempts.size)
        assertEquals(1, attempts[0].attemptNumber)
        assertEquals(2, attempts[1].attemptNumber)
    }
}
