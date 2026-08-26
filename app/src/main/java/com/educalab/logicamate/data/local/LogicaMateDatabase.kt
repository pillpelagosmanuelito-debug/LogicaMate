package com.educalab.logicamate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.educalab.logicamate.data.local.dao.AttemptDao
import com.educalab.logicamate.data.local.dao.CategoryDao
import com.educalab.logicamate.data.local.dao.ChallengeDao
import com.educalab.logicamate.data.local.dao.ChamberDao
import com.educalab.logicamate.data.local.dao.CollectibleDao
import com.educalab.logicamate.data.local.dao.DailyChallengeDao
import com.educalab.logicamate.data.local.dao.GamificationDao
import com.educalab.logicamate.data.local.dao.KeyFragmentDao
import com.educalab.logicamate.data.local.dao.ProgressDao
import com.educalab.logicamate.data.local.dao.UserProfileDao
import com.educalab.logicamate.data.local.entity.AttemptEntity
import com.educalab.logicamate.data.local.entity.BadgeEntity
import com.educalab.logicamate.data.local.entity.ChallengeEntity
import com.educalab.logicamate.data.local.entity.ChallengeItemEntity
import com.educalab.logicamate.data.local.entity.ChallengeRuleEntity
import com.educalab.logicamate.data.local.entity.CollectibleItemEntity
import com.educalab.logicamate.data.local.entity.DailyChallengeEntity
import com.educalab.logicamate.data.local.entity.HintEntity
import com.educalab.logicamate.data.local.entity.HintUsageEntity
import com.educalab.logicamate.data.local.entity.KeyFragmentEntity
import com.educalab.logicamate.data.local.entity.LogicCategoryEntity
import com.educalab.logicamate.data.local.entity.LogicChamberEntity
import com.educalab.logicamate.data.local.entity.ProgressEntity
import com.educalab.logicamate.data.local.entity.UnlockedCollectibleEntity
import com.educalab.logicamate.data.local.entity.UnlockedFragmentEntity
import com.educalab.logicamate.data.local.entity.UserBadgeEntity
import com.educalab.logicamate.data.local.entity.UserProfileEntity
import com.educalab.logicamate.data.local.entity.UserStatsEntity

@Database(
    entities = [
        UserProfileEntity::class,
        LogicChamberEntity::class,
        LogicCategoryEntity::class,
        ChallengeEntity::class,
        ChallengeItemEntity::class,
        ChallengeRuleEntity::class,
        HintEntity::class,
        AttemptEntity::class,
        HintUsageEntity::class,
        DailyChallengeEntity::class,
        KeyFragmentEntity::class,
        UnlockedFragmentEntity::class,
        ProgressEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class,
        CollectibleItemEntity::class,
        UnlockedCollectibleEntity::class,
        UserStatsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LogicaMateDatabase : RoomDatabase() {
    abstract fun chamberDao(): ChamberDao
    abstract fun categoryDao(): CategoryDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun attemptDao(): AttemptDao
    abstract fun progressDao(): ProgressDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun keyFragmentDao(): KeyFragmentDao
    abstract fun collectibleDao(): CollectibleDao
    abstract fun dailyChallengeDao(): DailyChallengeDao

    companion object {
        @Volatile private var INSTANCE: LogicaMateDatabase? = null

        fun getInstance(context: Context): LogicaMateDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LogicaMateDatabase::class.java,
                    "logicamate.db",
                ).build().also { INSTANCE = it }
            }
    }
}
