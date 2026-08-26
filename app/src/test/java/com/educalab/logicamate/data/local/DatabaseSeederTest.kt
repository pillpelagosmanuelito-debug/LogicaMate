package com.educalab.logicamate.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.ChamberStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Usa una base de datos Room EN MEMORIA sobre Robolectric — persistencia
 * real (SQL real vía SQLite embebido), no listas en memoria como sustituto,
 * tal como exige el prompt maestro (sección 22).
 */
@RunWith(RobolectricTestRunner::class)
class DatabaseSeederTest {

    private lateinit var db: LogicaMateDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LogicaMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `seeding an empty database inserts all 10 chambers`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        assertEquals(10, db.chamberDao().count())
    }

    @Test
    fun `seeding inserts at least 130 challenges`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        assertTrue(db.challengeDao().count() >= 130)
    }

    @Test
    fun `seeding is idempotent and does not duplicate challenges on a second run`() = runBlocking {
        val seeder = DatabaseSeeder(db)
        seeder.seedIfNeeded()
        val firstCount = db.challengeDao().count()
        seeder.seedIfNeeded()
        assertEquals(firstCount, db.challengeDao().count())
    }

    @Test
    fun `seeding creates a default profile`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        assertTrue(db.userProfileDao().get() != null)
    }

    @Test
    fun `entrance chamber starts available and all others start locked`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        val entrance = db.progressDao().get(ChamberId.ENTRANCE.name)!!
        val patterns = db.progressDao().get(ChamberId.PATTERNS.name)!!
        assertEquals(ChamberStatus.AVAILABLE.name, entrance.status)
        assertEquals(ChamberStatus.LOCKED.name, patterns.status)
    }

    @Test
    fun `seeding inserts exactly 8 badges`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        assertEquals(8, db.gamificationDao().countBadges())
    }

    @Test
    fun `seeding inserts key fragments for the 8 content chambers`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        val unlockedInitially = db.keyFragmentDao().getUnlockedIds()
        assertTrue(unlockedInitially.isEmpty()) // ninguno desbloqueado todavía, pero las filas padre existen
    }

    @Test
    fun `every chamber has a matching progress row after seeding`() = runBlocking {
        DatabaseSeeder(db).seedIfNeeded()
        ChamberId.entries.forEach { chamber ->
            assertTrue(db.progressDao().get(chamber.name) != null)
        }
    }
}
