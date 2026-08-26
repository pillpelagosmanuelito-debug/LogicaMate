package com.educalab.logicamate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.LogicCategory

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L, // perfil único local (sin cuentas, sección 32)
    val alias: String,
    val avatarId: Int, // índice 0-7 sobre 8 avatares base locales
    val createdAtMillis: Long,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
)

@Entity(tableName = "logic_chamber")
data class LogicChamberEntity(
    @PrimaryKey val id: String, // ChamberId.name
    val displayName: String,
    val orderIndex: Int,
    val iconRes: String,
    val flavorText: String,
)

@Entity(tableName = "logic_category")
data class LogicCategoryEntity(
    @PrimaryKey val id: String, // LogicCategory.name
    val displayName: String,
    val iconRes: String,
)

fun ChamberId.toEntity(displayName: String, iconRes: String, flavorText: String) = LogicChamberEntity(
    id = name,
    displayName = displayName,
    orderIndex = order,
    iconRes = iconRes,
    flavorText = flavorText,
)

fun LogicCategory.toEntity(displayName: String, iconRes: String) = LogicCategoryEntity(
    id = name,
    displayName = displayName,
    iconRes = iconRes,
)
