package com.educalab.logicamate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.logicamate.data.local.entity.ChallengeEntity
import com.educalab.logicamate.data.local.entity.ChallengeItemEntity
import com.educalab.logicamate.data.local.entity.ChallengeRuleEntity
import com.educalab.logicamate.data.local.entity.HintEntity
import com.educalab.logicamate.data.local.entity.LogicCategoryEntity
import com.educalab.logicamate.data.local.entity.LogicChamberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChamberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chambers: List<LogicChamberEntity>)

    @Query("SELECT * FROM logic_chamber ORDER BY orderIndex ASC")
    fun observeAll(): Flow<List<LogicChamberEntity>>

    @Query("SELECT * FROM logic_chamber WHERE id = :id")
    suspend fun getById(id: String): LogicChamberEntity?

    @Query("SELECT COUNT(*) FROM logic_chamber")
    suspend fun count(): Int
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<LogicCategoryEntity>)

    @Query("SELECT * FROM logic_category")
    fun observeAll(): Flow<List<LogicCategoryEntity>>

    @Query("SELECT COUNT(*) FROM logic_category")
    suspend fun count(): Int
}

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChallengeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<ChallengeRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHints(hints: List<HintEntity>)

    @Query("SELECT * FROM challenge WHERE chamberId = :chamberId ORDER BY orderIndex ASC")
    fun observeByChamber(chamberId: String): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenge WHERE chamberId = :chamberId ORDER BY orderIndex ASC")
    suspend fun getByChamber(chamberId: String): List<ChallengeEntity>

    @Query("SELECT * FROM challenge WHERE id = :id")
    suspend fun getById(id: String): ChallengeEntity?

    @Query("SELECT * FROM challenge_item WHERE challengeId = :challengeId ORDER BY position ASC")
    suspend fun getItems(challengeId: String): List<ChallengeItemEntity>

    @Query("SELECT * FROM challenge_rule WHERE challengeId = :challengeId")
    suspend fun getRules(challengeId: String): List<ChallengeRuleEntity>

    @Query("SELECT * FROM hint WHERE challengeId = :challengeId ORDER BY level ASC")
    suspend fun getHints(challengeId: String): List<HintEntity>

    @Query("SELECT COUNT(*) FROM challenge")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM challenge WHERE chamberId = :chamberId")
    suspend fun countByChamber(chamberId: String): Int

    @Query("SELECT COUNT(*) FROM challenge WHERE interactionType = :interactionType")
    suspend fun countByInteractionType(interactionType: String): Int

    /** Elige un desafío aleatorio no ligado a una cámara concreta, para el Reto Diario. */
    @Query("SELECT * FROM challenge WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomExcluding(excludeIds: List<String>): ChallengeEntity?
}
