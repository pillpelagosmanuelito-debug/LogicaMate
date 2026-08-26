package com.educalab.logicamate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.educalab.logicamate.data.local.entity.AttemptEntity
import com.educalab.logicamate.data.local.entity.BadgeEntity
import com.educalab.logicamate.data.local.entity.CollectibleItemEntity
import com.educalab.logicamate.data.local.entity.DailyChallengeEntity
import com.educalab.logicamate.data.local.entity.HintUsageEntity
import com.educalab.logicamate.data.local.entity.KeyFragmentEntity
import com.educalab.logicamate.data.local.entity.ProgressEntity
import com.educalab.logicamate.data.local.entity.UnlockedCollectibleEntity
import com.educalab.logicamate.data.local.entity.UnlockedFragmentEntity
import com.educalab.logicamate.data.local.entity.UserBadgeEntity
import com.educalab.logicamate.data.local.entity.UserProfileEntity
import com.educalab.logicamate.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = :id")
    fun observe(id: Long = 1L): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun get(id: Long = 1L): UserProfileEntity?
}

@Dao
interface AttemptDao {
    @Insert
    suspend fun insert(attempt: AttemptEntity): Long

    @Insert
    suspend fun insertHintUsage(usage: HintUsageEntity)

    @Query("SELECT * FROM attempt WHERE challengeId = :challengeId ORDER BY startedAtMillis ASC")
    suspend fun getForChallenge(challengeId: String): List<AttemptEntity>

    @Query("SELECT COUNT(*) FROM attempt WHERE challengeId = :challengeId")
    suspend fun countForChallenge(challengeId: String): Int

    @Query("SELECT COUNT(*) FROM attempt WHERE userProfileId = :userProfileId AND isCorrect = 1")
    suspend fun countCorrectForUser(userProfileId: Long): Int

    @Query(
        """SELECT COUNT(*) FROM attempt a
           JOIN challenge c ON a.challengeId = c.id
           WHERE a.userProfileId = :userProfileId AND a.isCorrect = 1 AND c.categoryId = :categoryId""",
    )
    suspend fun countCorrectByCategory(userProfileId: Long, categoryId: String): Int

    @Query(
        """SELECT COUNT(*) FROM attempt
           WHERE userProfileId = :userProfileId AND isCorrect = 1 AND attemptNumber = 1 AND hintsUsedCount = 0""",
    )
    suspend fun countPerfectForUser(userProfileId: Long): Int
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Query("SELECT * FROM progress WHERE userProfileId = :userProfileId")
    fun observeAll(userProfileId: Long = 1L): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE userProfileId = :userProfileId AND chamberId = :chamberId")
    suspend fun get(chamberId: String, userProfileId: Long = 1L): ProgressEntity?
}

@Dao
interface GamificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE userProfileId = :userProfileId")
    fun observeStats(userProfileId: Long = 1L): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE userProfileId = :userProfileId")
    suspend fun getStats(userProfileId: Long = 1L): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Query("SELECT * FROM badge")
    fun observeAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT COUNT(*) FROM badge")
    suspend fun countBadges(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockBadge(userBadge: UserBadgeEntity)

    @Query("SELECT badgeId FROM user_badge WHERE userProfileId = :userProfileId")
    suspend fun getUnlockedBadgeIds(userProfileId: Long = 1L): List<String>

    @Query("SELECT * FROM user_badge WHERE userProfileId = :userProfileId")
    fun observeUnlockedBadges(userProfileId: Long = 1L): Flow<List<UserBadgeEntity>>
}

@Dao
interface KeyFragmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fragments: List<KeyFragmentEntity>)

    @Query("SELECT * FROM key_fragment ORDER BY orderIndex ASC")
    fun observeAll(): Flow<List<KeyFragmentEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(unlocked: UnlockedFragmentEntity)

    @Query("SELECT keyFragmentId FROM unlocked_fragment WHERE userProfileId = :userProfileId")
    suspend fun getUnlockedIds(userProfileId: Long = 1L): List<String>

    @Query("SELECT * FROM unlocked_fragment WHERE userProfileId = :userProfileId")
    fun observeUnlocked(userProfileId: Long = 1L): Flow<List<UnlockedFragmentEntity>>
}

@Dao
interface CollectibleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CollectibleItemEntity>)

    @Query("SELECT * FROM collectible_item")
    fun observeAll(): Flow<List<CollectibleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(unlocked: UnlockedCollectibleEntity)

    @Query("SELECT collectibleItemId FROM unlocked_collectible WHERE userProfileId = :userProfileId")
    suspend fun getUnlockedIds(userProfileId: Long = 1L): List<String>
}

@Dao
interface DailyChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(daily: DailyChallengeEntity)

    @Query("SELECT * FROM daily_challenge WHERE date = :date")
    suspend fun getForDate(date: String): DailyChallengeEntity?

    @Query("SELECT * FROM daily_challenge WHERE date = :date")
    fun observeForDate(date: String): Flow<DailyChallengeEntity?>

    @Query("SELECT date FROM daily_challenge WHERE completed = 1")
    suspend fun getCompletedDates(): List<String>
}
