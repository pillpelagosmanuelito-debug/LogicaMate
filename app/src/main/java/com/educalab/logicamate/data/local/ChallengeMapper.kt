package com.educalab.logicamate.data.local

import com.educalab.logicamate.data.local.entity.ChallengeEntity
import com.educalab.logicamate.data.local.entity.ChallengeItemEntity
import com.educalab.logicamate.data.local.entity.ChallengeRuleEntity
import com.educalab.logicamate.data.local.entity.HintEntity
import com.educalab.logicamate.domain.model.Challenge
import com.educalab.logicamate.domain.model.ChamberId
import com.educalab.logicamate.domain.model.DifficultyLevel
import com.educalab.logicamate.domain.model.Hint
import com.educalab.logicamate.domain.model.InteractionType
import com.educalab.logicamate.domain.model.LogicCategory
import com.educalab.logicamate.domain.model.LogicRule
import com.educalab.logicamate.domain.model.PieceSpec

/** Resultado de aplanar un [Challenge] de dominio a filas de Room. */
data class ChallengeRows(
    val challenge: ChallengeEntity,
    val items: List<ChallengeItemEntity>,
    val rules: List<ChallengeRuleEntity>,
    val hints: List<HintEntity>,
)

fun Challenge.toRows(orderIndex: Int): ChallengeRows {
    val challengeEntity = ChallengeEntity(
        id = id,
        chamberId = chamberId.name,
        categoryId = category.name,
        difficulty = difficulty.name,
        interactionType = interactionType.name,
        prompt = prompt,
        explanation = explanation,
        isSeed = isSeed,
        orderIndex = orderIndex,
    )
    val displayItems = items.mapIndexed { idx, piece ->
        ChallengeItemEntity(challengeId = id, position = idx, pieceEncoded = piece.encode(), role = "DISPLAY")
    }
    val optionItems = optionPool.mapIndexed { idx, piece ->
        ChallengeItemEntity(challengeId = id, position = idx, pieceEncoded = piece.encode(), role = "OPTION")
    }
    val solutionItems = solutionPieces.mapIndexed { idx, piece ->
        ChallengeItemEntity(challengeId = id, position = solutionIndices.getOrElse(idx) { idx }, pieceEncoded = piece.encode(), role = "SOLUTION")
    }
    val ruleEntity = ChallengeRuleEntity(
        challengeId = id,
        ruleType = rule.type,
        paramsEncoded = rule.params.entries.joinToString(";") { "${it.key}=${it.value}" },
    )
    val hintEntities = hints.map { HintEntity(challengeId = id, level = it.level, text = it.text) }
    return ChallengeRows(challengeEntity, displayItems + optionItems + solutionItems, listOf(ruleEntity), hintEntities)
}

fun decodeParams(encoded: String): Map<String, String> =
    if (encoded.isBlank()) emptyMap()
    else encoded.split(";").associate { pair ->
        val (k, v) = pair.split("=", limit = 2)
        k to v
    }

fun fromRows(
    entity: ChallengeEntity,
    items: List<ChallengeItemEntity>,
    rules: List<ChallengeRuleEntity>,
    hints: List<HintEntity>,
): Challenge {
    val display = items.filter { it.role == "DISPLAY" }.sortedBy { it.position }.map { PieceSpec.decode(it.pieceEncoded) }
    val options = items.filter { it.role == "OPTION" }.sortedBy { it.position }.map { PieceSpec.decode(it.pieceEncoded) }
    val solutionRows = items.filter { it.role == "SOLUTION" }.sortedBy { it.position }
    val ruleRow = rules.first()
    return Challenge(
        id = entity.id,
        chamberId = ChamberId.valueOf(entity.chamberId),
        category = LogicCategory.valueOf(entity.categoryId),
        difficulty = DifficultyLevel.valueOf(entity.difficulty),
        interactionType = InteractionType.valueOf(entity.interactionType),
        prompt = entity.prompt,
        items = display,
        optionPool = options,
        solutionIndices = solutionRows.map { it.position },
        solutionPieces = solutionRows.map { PieceSpec.decode(it.pieceEncoded) },
        rule = LogicRule(ruleRow.ruleType, decodeParams(ruleRow.paramsEncoded)),
        hints = hints.map { Hint(it.level, it.text) },
        explanation = entity.explanation,
        isSeed = entity.isSeed,
    )
}
