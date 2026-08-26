package com.educalab.logicamate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "challenge",
    foreignKeys = [
        ForeignKey(entity = LogicChamberEntity::class, parentColumns = ["id"], childColumns = ["chamberId"]),
        ForeignKey(entity = LogicCategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"]),
    ],
    indices = [Index("chamberId"), Index("categoryId"), Index("difficulty")],
)
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val chamberId: String,
    val categoryId: String,
    val difficulty: String,        // DifficultyLevel.name
    val interactionType: String,   // InteractionType.name
    val prompt: String,
    val explanation: String,
    val isSeed: Boolean,
    val orderIndex: Int, // orden sugerido dentro de la cámara
)

/**
 * Una pieza individual dentro de un desafío. Separada de ChallengeEntity
 * (en vez de un único blob JSON) para poder consultar/renderizar piezas de
 * forma independiente en la UI de arrastrar-y-soltar (sección 34).
 */
@Entity(
    tableName = "challenge_item",
    foreignKeys = [ForeignKey(entity = ChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("challengeId")],
)
data class ChallengeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val position: Int,             // posición dentro de la secuencia/tira/matriz mostrada
    val pieceEncoded: String,      // PieceSpec.encode()
    val role: String,              // "DISPLAY" (parte del enunciado) | "OPTION" (pieza arrastrable) | "SOLUTION"
)

/**
 * Metadatos de la regla que generó/valida el desafío (tipo + parámetros
 * serializados). Permite reconstruir el motor correcto (SequenceEngine,
 * MatrixEngine.CellTransform, etc.) sin tener que volver a adivinar la regla.
 */
@Entity(
    tableName = "challenge_rule",
    foreignKeys = [ForeignKey(entity = ChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("challengeId")],
)
data class ChallengeRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val ruleType: String,
    val paramsEncoded: String, // "k1=v1;k2=v2"
)

@Entity(
    tableName = "hint",
    foreignKeys = [ForeignKey(entity = ChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("challengeId")],
)
data class HintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val level: Int, // 1-3
    val text: String,
)
