package com.educalab.logicamate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attempt",
    foreignKeys = [ForeignKey(entity = ChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("challengeId"), Index("userProfileId")],
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val userProfileId: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val isCorrect: Boolean,
    val attemptNumber: Int,      // 1 = primer intento sobre este desafío
    val hintsUsedCount: Int,
    val submittedSolutionEncoded: String,
)

@Entity(
    tableName = "hint_usage",
    foreignKeys = [ForeignKey(entity = AttemptEntity::class, parentColumns = ["id"], childColumns = ["attemptId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("attemptId")],
)
data class HintUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val hintLevel: Int,
    val usedAtMillis: Long,
)

@Entity(
    tableName = "daily_challenge",
    foreignKeys = [ForeignKey(entity = ChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"])],
    indices = [Index("challengeId")],
)
data class DailyChallengeEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd" local
    val challengeId: String,
    val completed: Boolean,
    val completedAtMillis: Long?,
)

@Entity(
    tableName = "key_fragment",
    foreignKeys = [ForeignKey(entity = LogicChamberEntity::class, parentColumns = ["id"], childColumns = ["chamberId"])],
    indices = [Index("chamberId")],
)
data class KeyFragmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val chamberId: String, // cámara cuya finalización otorga este fragmento
    val orderIndex: Int,
    val shapeDescriptor: String, // referencia visual (ic_key_fragment + tint)
)

@Entity(
    tableName = "unlocked_fragment",
    foreignKeys = [ForeignKey(entity = KeyFragmentEntity::class, parentColumns = ["id"], childColumns = ["keyFragmentId"])],
    indices = [Index("keyFragmentId"), Index("userProfileId")],
)
data class UnlockedFragmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userProfileId: Long,
    val keyFragmentId: String,
    val unlockedAtMillis: Long,
)

@Entity(
    tableName = "progress",
    foreignKeys = [ForeignKey(entity = LogicChamberEntity::class, parentColumns = ["id"], childColumns = ["chamberId"])],
    indices = [Index("chamberId"), Index("userProfileId")],
    primaryKeys = ["userProfileId", "chamberId"],
)
data class ProgressEntity(
    val userProfileId: Long,
    val chamberId: String,
    val status: String, // ChamberStatus.name
    val challengesCompleted: Int,
    val perfectChallenges: Int,
    val totalChallenges: Int,
    val xpEarnedInChamber: Int,
)

@Entity(tableName = "badge")
data class BadgeEntity(
    @PrimaryKey val id: String, // GamificationEngine.BadgeCode.name
    val name: String,
    val description: String,
    val iconRes: String,
    val criteriaDescription: String,
)

@Entity(
    tableName = "user_badge",
    foreignKeys = [ForeignKey(entity = BadgeEntity::class, parentColumns = ["id"], childColumns = ["badgeId"])],
    indices = [Index("badgeId"), Index("userProfileId")],
)
data class UserBadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userProfileId: Long,
    val badgeId: String,
    val unlockedAtMillis: Long,
)

@Entity(tableName = "collectible_item")
data class CollectibleItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val chamberId: String,
    val iconRes: String,
)

@Entity(
    tableName = "unlocked_collectible",
    foreignKeys = [ForeignKey(entity = CollectibleItemEntity::class, parentColumns = ["id"], childColumns = ["collectibleItemId"])],
    indices = [Index("collectibleItemId"), Index("userProfileId")],
)
data class UnlockedCollectibleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userProfileId: Long,
    val collectibleItemId: String,
    val unlockedAtMillis: Long,
)

/** XP y racha global del usuario — separado de Progress (que es por cámara). */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userProfileId: Long,
    val totalXp: Int,
    val currentStreak: Int,
    val lastActiveDay: Long?, // GamificationEngine.dayIndex()
)
